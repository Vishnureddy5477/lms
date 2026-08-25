-- ============================================================
--  MCQ MODULE TEST — REFRAME MIGRATION   (PRODUCTION)
--
--  100% ADDITIVE. Read this before running:
--    * No existing row is UPDATEd or DELETEd by this script.
--    * No existing column is dropped, renamed or retyped.
--    * The legacy JSP portal keeps writing to moduletestanswer /
--      moduletestresult exactly as it does today. Both systems
--      coexist; the compatibility view in PART F unions them.
--
--  Everything here is either a new column with a default, a new
--  table, a new index, or a view. Safe to run during low activity.
--  Each statement is independently re-runnable-ish: if you see
--  "Duplicate column name" / "Duplicate key name" / "Table exists",
--  that statement already ran — skip it and continue.
-- ============================================================


-- ============================================================
--  PART A — SESSION ROW: attempt number + proctoring state
--
--  moduletestlogin.id is the session/attempt identity that every
--  other table hangs off (the legacy code calls it "exam_id").
--
--  attempt_no is the important one. The legacy submit page computed
--  the attempt as MAX(attempt)+1 AT SUBMIT TIME, which is why a
--  refresh of the results page could write another result row
--  (3,770 duplicate groups in production today). Fixing the attempt
--  number at session START makes the number stable for the whole
--  attempt, so a resubmit targets the SAME result row.
-- ============================================================

ALTER TABLE cranescrm.moduletestlogin
  ADD COLUMN attempt_no      INT        NULL     DEFAULT NULL COMMENT 'attempt number, fixed at session start',
  ADD COLUMN violation_count INT        NOT NULL DEFAULT 0    COMMENT 'proctoring strikes, server-authoritative',
  ADD COLUMN auto_submitted  TINYINT(1) NOT NULL DEFAULT 0    COMMENT '1 = auto-submitted (strikes or timer)';

-- Supports the anti-repeat history lookup: "which questions has this
-- student already been served for this module + test number?"
ALTER TABLE cranescrm.moduletestlogin
  ADD INDEX idx_history (regno, module, no_of_test);


-- ============================================================
--  PART B — PROCTORING AUDIT TRAIL
--
--  violation_count on the session row is the running total the
--  engine enforces against. This table is the evidence behind it:
--  when a student disputes an auto-submit, this is what a trainer
--  reads. One row per strike, never updated.
-- ============================================================

CREATE TABLE IF NOT EXISTS cranescrm.module_test_violation (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  session_id  INT          NOT NULL COMMENT 'cranescrm.moduletestlogin.id',
  type        VARCHAR(60)  NOT NULL COMMENT 'tab-switch | window-blur | fullscreen-exit | devtools',
  reason      VARCHAR(255) NOT NULL,
  occurred_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
--  PART C — PER-ATTEMPT QUESTION SNAPSHOT (narrow)
--
--  Replaces moduletestquestions_assigned for NEW attempts. Same
--  proven idea (freeze the 40 picks so a refresh cannot reshuffle),
--  but cheap: no auto-inc id, no redundant unique key. The old table
--  carried 17MB of data under 28MB of index for exactly this.
--
--  PRIMARY KEY is (session_id, question_order) — NOT question_id.
--  A thin question bank may have to backfill a duplicate question to
--  reach 40 slots, so the same question_id can legitimately occupy
--  two slots. The SLOT is the identity, not the question.
-- ============================================================

CREATE TABLE IF NOT EXISTS exam_system.module_test_question_set (
  session_id     INT      NOT NULL COMMENT 'cranescrm.moduletestlogin.id',
  question_id    INT      NOT NULL COMMENT 'exam_system.moduletestquestions.id',
  question_order SMALLINT NOT NULL COMMENT '1..40, the slot the student sees',
  PRIMARY KEY (session_id, question_order),
  KEY idx_session_question (session_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
--  PART D — PER-QUESTION ANSWER LOG (narrow)
--
--  The legacy moduletestanswer stores the full question longtext,
--  the correct-answer longtext and three varchar(500) columns on
--  EVERY row — 1.55M rows / 344MB, nearly all of it derivable by a
--  join. This is the same information at ~20 bytes a row.
--
--  Keyed by SLOT (session_id, question_order) for the same reason
--  as PART C: two slots holding a backfilled duplicate question must
--  be answerable independently.
--
--  selected_option: 0 = not attempted, 1..4 = the chosen option.
--  is_correct is decided by the SERVER at save time by reading the
--  answer key itself — it is never sent by, or exposed to, the client.
-- ============================================================

CREATE TABLE IF NOT EXISTS exam_system.module_test_answer (
  session_id      INT       NOT NULL COMMENT 'cranescrm.moduletestlogin.id',
  question_order  SMALLINT  NOT NULL COMMENT 'slot in module_test_question_set',
  question_id     INT       NOT NULL,
  selected_option TINYINT   NOT NULL DEFAULT 0 COMMENT '0=not attempted, 1..4',
  is_correct      TINYINT(1) NOT NULL DEFAULT 0,
  answered_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (session_id, question_order),
  KEY idx_session_question (session_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
--  PART E — IDEMPOTENT RESULTS, WITHOUT TOUCHING HISTORY
--
--  We CANNOT add UNIQUE(reg, module, no_of_test, batchno, attempt):
--  3,770 groups in the existing data already violate it (the refresh
--  -resubmit bug), and de-duplicating them would modify old data.
--
--  Instead: a nullable session_id with a UNIQUE index. MySQL permits
--  unlimited NULLs in a unique index, so all 57K legacy rows keep
--  NULL and are completely unaffected, while every NEW attempt writes
--  its session id and is protected at the database level — a resubmit
--  UPDATEs its own row and physically cannot insert a second one.
-- ============================================================

ALTER TABLE exam_system.moduletestresult
  ADD COLUMN session_id INT NULL DEFAULT NULL COMMENT 'cranescrm.moduletestlogin.id; NULL for legacy rows',
  ADD UNIQUE KEY uq_result_session (session_id);


-- ============================================================
--  PART F — COMPATIBILITY VIEW
--
--  Existing reports (mcq-test-result-details, Skill Tracker, report
--  card) read moduletestanswer and expect its fat column set. This
--  view presents old fat rows and new narrow rows under exactly those
--  column names, so those reports keep working with no query changes
--  — the Spring DAO simply points here instead of at the table.
--
--  The physical moduletestanswer table is deliberately NOT renamed:
--  the live legacy JSP portal still writes to it.
-- ============================================================

CREATE OR REPLACE VIEW exam_system.v_module_test_answer AS
  -- legacy rows, exactly as they are on disk
  SELECT
      a.regno                              AS regno,
      a.exam_id                            AS exam_id,
      a.question_id                        AS question_id,
      a.answer_id                          AS answer_id,
      a.subject                            AS subject,
      a.no_of_test                         AS no_of_test,
      a.test                               AS test,
      a.question                           AS question,
      a.answer                             AS answer,
      a.correct_answer                     AS correct_answer,
      a.status                             AS status,
      a.attempt                            AS attempt,
      a.datetime                           AS datetime
  FROM exam_system.moduletestanswer a

  UNION ALL

  -- new narrow rows, re-expanded by joining what was previously duplicated
  SELECT
      l.regno                              AS regno,
      n.session_id                         AS exam_id,
      n.question_id                        AS question_id,
      n.selected_option                    AS answer_id,
      l.module                             AS subject,
      CAST(l.no_of_test AS CHAR)           AS no_of_test,
      l.test                               AS test,
      q.questions                          AS question,
      CAST(n.selected_option AS CHAR)      AS answer,
      CASE q.correct_option
          WHEN 1 THEN q.opt1 WHEN 2 THEN q.opt2
          WHEN 3 THEN q.opt3 WHEN 4 THEN q.opt4
      END                                  AS correct_answer,
      CASE
          WHEN n.selected_option = 0 THEN 'NOT_ATTEMPTED'
          WHEN n.is_correct      = 1 THEN 'Correct'
          ELSE                            'Incorrect'
      END                                  AS status,
      l.attempt_no                         AS attempt,
      n.answered_at                        AS datetime
  FROM exam_system.module_test_answer n
  JOIN cranescrm.moduletestlogin      l ON l.id = n.session_id
  LEFT JOIN exam_system.moduletestquestions q ON q.id = n.question_id;


-- ============================================================
--  VERIFICATION — all read-only, run after the statements above.
-- ============================================================

-- New columns present on the session row?
-- SHOW COLUMNS FROM cranescrm.moduletestlogin LIKE '%attempt_no%';
-- SHOW COLUMNS FROM cranescrm.moduletestlogin LIKE '%violation%';

-- Unique key present, and every legacy row still NULL (must equal the
-- full table count — proves PART E modified nothing)?
-- SELECT COUNT(*) AS total, SUM(session_id IS NULL) AS legacy_untouched
--   FROM exam_system.moduletestresult;

-- View readable, and legacy history intact through it?
-- SELECT COUNT(*) FROM exam_system.v_module_test_answer;   -- >= 1,547,396
