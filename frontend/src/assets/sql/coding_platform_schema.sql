-- ==================================================================
-- Coding Platform — Database Schema
-- MySQL 8.0+ / MariaDB 10.5+
-- ==================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS test_cases;
DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS difficulty_levels;
DROP TABLE IF EXISTS topics;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------
-- 1. topics
-- ------------------------------------------------------------------
CREATE TABLE topics (
  id            INT           NOT NULL AUTO_INCREMENT,
  name          VARCHAR(100)  NOT NULL,
  description   TEXT              NULL,
  icon_class    VARCHAR(60)   NOT NULL DEFAULT 'bi bi-code-slash',
  display_order INT           NOT NULL DEFAULT 0,
  is_active     TINYINT(1)    NOT NULL DEFAULT 1,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_topics_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 2. difficulty_levels
-- ------------------------------------------------------------------
CREATE TABLE difficulty_levels (
  id          INT         NOT NULL AUTO_INCREMENT,
  name        VARCHAR(20) NOT NULL,
  badge_color VARCHAR(20) NOT NULL COMMENT 'Bootstrap color: success | warning | danger',
  created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_difficulty_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 3. problems
-- ------------------------------------------------------------------
CREATE TABLE problems (
  id                INT          NOT NULL AUTO_INCREMENT,
  title             VARCHAR(200) NOT NULL,
  slug              VARCHAR(200) NOT NULL COMMENT 'URL-friendly unique identifier',
  description       TEXT             NULL,
  topic_id          INT          NOT NULL,
  difficulty_id     INT          NOT NULL,
  input_format      TEXT             NULL,
  output_format     TEXT             NULL,
  constraints_text  TEXT             NULL,
  is_active         TINYINT(1)   NOT NULL DEFAULT 1,
  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_problems_slug (slug),
  KEY idx_problems_topic      (topic_id),
  KEY idx_problems_difficulty (difficulty_id),
  KEY idx_problems_active     (is_active),
  CONSTRAINT fk_problems_topic
    FOREIGN KEY (topic_id)      REFERENCES topics (id),
  CONSTRAINT fk_problems_difficulty
    FOREIGN KEY (difficulty_id) REFERENCES difficulty_levels (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 4. test_cases
-- ------------------------------------------------------------------
CREATE TABLE test_cases (
  id               INT                              NOT NULL AUTO_INCREMENT,
  problem_id       INT                              NOT NULL,
  case_type        ENUM('sample', 'hidden', 'edge') NOT NULL
                   COMMENT 'sample=shown to user; hidden/edge=server-side only',
  input_data       TEXT                             NOT NULL,
  expected_output  TEXT                             NOT NULL,
  explanation      TEXT                                 NULL
                   COMMENT 'Only populated for sample cases; NULL for hidden/edge',
  time_limit_ms    INT                              NOT NULL DEFAULT 2000,
  memory_limit_kb  INT                              NOT NULL DEFAULT 256000,
  order_no         INT                              NOT NULL DEFAULT 1,
  is_active        TINYINT(1)                       NOT NULL DEFAULT 1,
  created_at       TIMESTAMP                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tc_problem  (problem_id),
  KEY idx_tc_type     (case_type),
  KEY idx_tc_active   (is_active),
  CONSTRAINT fk_test_cases_problem
    FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==================================================================
-- SEED DATA
-- ==================================================================

-- ------------------------------------------------------------------
-- Topics (15 rows)
-- ------------------------------------------------------------------
INSERT INTO topics (id, name, description, icon_class, display_order) VALUES
( 1, 'Basic Programming & Control Flow',
     'Fundamental constructs: variables, loops, conditionals, and control-flow statements.',
     'bi bi-cpu',                    1),
( 2, 'Arrays',
     'Single and multi-dimensional arrays: traversal, searching, and manipulation.',
     'bi bi-grid',                   2),
( 3, 'Strings',
     'String manipulation, pattern matching, and character-level operations.',
     'bi bi-fonts',                  3),
( 4, 'Linked List',
     'Singly and doubly linked lists: insertion, deletion, traversal, and reversal.',
     'bi bi-link',                   4),
( 5, 'Stack & Queue',
     'LIFO and FIFO data structures and their real-world applications.',
     'bi bi-stack',                  5),
( 6, 'Trees',
     'Binary trees, BST, AVL trees, and tree-traversal algorithms.',
     'bi bi-diagram-3',              6),
( 7, 'Graphs',
     'Graph representations, BFS, DFS, shortest path, and spanning-tree algorithms.',
     'bi bi-share',                  7),
( 8, 'Sorting',
     'Comparison and non-comparison based sorting algorithms.',
     'bi bi-sort-down',              8),
( 9, 'Searching',
     'Linear search, binary search, and advanced searching techniques.',
     'bi bi-search',                 9),
(10, 'Dynamic Programming',
     'Memoization and tabulation-based optimisation problems.',
     'bi bi-lightning',             10),
(11, 'Recursion',
     'Recursive problem solving, backtracking, and divide-and-conquer.',
     'bi bi-arrow-repeat',          11),
(12, 'OOP - Class & Object',
     'Classes, objects, inheritance, polymorphism, and encapsulation.',
     'bi bi-box',                   12),
(13, 'Exception Handling',
     'Error handling, try-catch blocks, and custom exception classes.',
     'bi bi-exclamation-triangle',  13),
(14, 'STL',
     'Standard Template Library: containers, iterators, and algorithms.',
     'bi bi-collection',            14),
(15, 'Templates',
     'Function templates and class templates for generic programming.',
     'bi bi-layout-text-window',    15);

-- ------------------------------------------------------------------
-- Difficulty Levels (3 rows)
-- ------------------------------------------------------------------
INSERT INTO difficulty_levels (id, name, badge_color) VALUES
(1, 'Beginner',     'success'),
(2, 'Intermediate', 'warning'),
(3, 'Advanced',     'danger');

-- ------------------------------------------------------------------
-- Problems (5 rows — topic_id=1 Basic Programming, difficulty_id=1 Beginner)
-- ------------------------------------------------------------------
INSERT INTO problems (id, title, slug, description, topic_id, difficulty_id,
                      input_format, output_format, constraints_text) VALUES

(1,
 'Decimal to Binary Conversion',
 'decimal-to-binary-conversion',
 'Given a non-negative decimal integer N, convert it to its binary representation.\n\nPrint the binary equivalent as a string of 0s and 1s without any leading zeros (except for the number 0 itself, which should print as "0").',
 1, 1,
 'A single line containing one non-negative integer N.',
 'A single line containing the binary representation of N.',
 '0 <= N <= 1,000,000,000'),

(2,
 'Check Prime Number',
 'check-prime-number',
 'Given a positive integer N, determine whether it is a prime number.\n\nA prime number is a natural number greater than 1 that has no positive divisors other than 1 and itself.\n\nPrint "YES" if N is prime, otherwise print "NO".',
 1, 1,
 'A single line containing one positive integer N.',
 'A single line: "YES" if N is prime, "NO" otherwise.',
 '1 <= N <= 1,000,000'),

(3,
 'Find Factorial of a Number',
 'find-factorial-of-a-number',
 'Given a non-negative integer N, compute its factorial.\n\nThe factorial of N (written N!) is the product of all positive integers from 1 to N.\nBy mathematical convention, 0! = 1.',
 1, 1,
 'A single line containing one non-negative integer N.',
 'A single line containing the factorial value of N.',
 '0 <= N <= 12'),

(4,
 'Fibonacci Series',
 'fibonacci-series',
 'Given a positive integer N, print the first N numbers of the Fibonacci series.\n\nThe Fibonacci series begins with 0 and 1. Every subsequent number is the sum of the two preceding numbers:\n  0, 1, 1, 2, 3, 5, 8, 13, 21, ...',
 1, 1,
 'A single line containing one positive integer N.',
 'A single line containing the first N Fibonacci numbers separated by single spaces.',
 '1 <= N <= 30'),

(5,
 'Check Palindrome Number',
 'check-palindrome-number',
 'Given an integer N, determine whether it reads the same forwards and backwards (i.e., is a palindrome).\n\nNegative numbers are never palindromes.\nPrint "YES" if N is a palindrome, otherwise "NO".',
 1, 1,
 'A single line containing one integer N.',
 'A single line: "YES" if N is a palindrome number, "NO" otherwise.',
 '-1,000,000,000 <= N <= 1,000,000,000');

-- ------------------------------------------------------------------
-- Test Cases  (7 per problem = 35 rows total)
-- Layout per problem: 2 sample → 3 hidden → 2 edge
-- explanation is NULL for hidden/edge (server-side only)
-- ------------------------------------------------------------------
INSERT INTO test_cases
  (problem_id, case_type, input_data, expected_output, explanation,
   time_limit_ms, memory_limit_kb, order_no)
VALUES

-- ── Problem 1: Decimal to Binary Conversion ──────────────────────
(1, 'sample', '10',  '1010',
 '10 ÷ 2 = 5 R0 → 5 ÷ 2 = 2 R1 → 2 ÷ 2 = 1 R0 → 1 ÷ 2 = 0 R1. Reading the remainders bottom-to-top gives 1010.',
 2000, 256000, 1),

(1, 'sample', '5',   '101',
 '5 ÷ 2 = 2 R1 → 2 ÷ 2 = 1 R0 → 1 ÷ 2 = 0 R1. Reading remainders bottom-to-top gives 101.',
 2000, 256000, 2),

(1, 'hidden', '255',  '11111111',   NULL, 2000, 256000, 3),
(1, 'hidden', '128',  '10000000',   NULL, 2000, 256000, 4),
(1, 'hidden', '64',   '1000000',    NULL, 2000, 256000, 5),

(1, 'edge',   '0',   '0',           NULL, 2000, 256000, 6),
(1, 'edge',   '1',   '1',           NULL, 2000, 256000, 7),

-- ── Problem 2: Check Prime Number ────────────────────────────────
(2, 'sample', '7',   'YES',
 '7 is divisible only by 1 and itself, so it is prime.',
 2000, 256000, 1),

(2, 'sample', '4',   'NO',
 '4 = 2 × 2, so it has a divisor other than 1 and itself — not prime.',
 2000, 256000, 2),

(2, 'hidden', '97',  'YES',         NULL, 2000, 256000, 3),
(2, 'hidden', '100', 'NO',          NULL, 2000, 256000, 4),
(2, 'hidden', '37',  'YES',         NULL, 2000, 256000, 5),

(2, 'edge',   '1',   'NO',          NULL, 2000, 256000, 6),
(2, 'edge',   '2',   'YES',         NULL, 2000, 256000, 7),

-- ── Problem 3: Find Factorial of a Number ────────────────────────
(3, 'sample', '5',  '120',
 '5! = 5 × 4 × 3 × 2 × 1 = 120.',
 2000, 256000, 1),

(3, 'sample', '3',  '6',
 '3! = 3 × 2 × 1 = 6.',
 2000, 256000, 2),

(3, 'hidden', '10', '3628800',      NULL, 2000, 256000, 3),
(3, 'hidden', '7',  '5040',         NULL, 2000, 256000, 4),
(3, 'hidden', '12', '479001600',    NULL, 2000, 256000, 5),

(3, 'edge',   '0',  '1',            NULL, 2000, 256000, 6),
(3, 'edge',   '1',  '1',            NULL, 2000, 256000, 7),

-- ── Problem 4: Fibonacci Series ──────────────────────────────────
(4, 'sample', '5',  '0 1 1 2 3',
 'First 5 Fibonacci numbers: F(0)=0, F(1)=1, F(2)=1, F(3)=2, F(4)=3.',
 2000, 256000, 1),

(4, 'sample', '7',  '0 1 1 2 3 5 8',
 'First 7 Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8.',
 2000, 256000, 2),

(4, 'hidden', '10', '0 1 1 2 3 5 8 13 21 34',         NULL, 2000, 256000, 3),
(4, 'hidden', '1',  '0',                               NULL, 2000, 256000, 4),
(4, 'hidden', '15', '0 1 1 2 3 5 8 13 21 34 55 89 144 233 377',
                                                        NULL, 2000, 256000, 5),

(4, 'edge',   '2',  '0 1',                             NULL, 2000, 256000, 6),
(4, 'edge',   '20', '0 1 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181',
                                                        NULL, 2000, 256000, 7),

-- ── Problem 5: Check Palindrome Number ───────────────────────────
(5, 'sample', '121',  'YES',
 '121 reversed is 121, which equals the original — it is a palindrome.',
 2000, 256000, 1),

(5, 'sample', '123',  'NO',
 '123 reversed is 321, which does not equal the original — not a palindrome.',
 2000, 256000, 2),

(5, 'hidden', '12321', 'YES',        NULL, 2000, 256000, 3),
(5, 'hidden', '12345', 'NO',         NULL, 2000, 256000, 4),
(5, 'hidden', '1001',  'YES',        NULL, 2000, 256000, 5),

(5, 'edge',   '0',     'YES',        NULL, 2000, 256000, 6),
(5, 'edge',   '-121',  'NO',         NULL, 2000, 256000, 7);


-- ==================================================================
-- Useful queries (commented out — run manually as needed)
-- ==================================================================
-- Count problems per topic:
-- SELECT t.name, COUNT(p.id) AS problem_count
-- FROM topics t LEFT JOIN problems p ON p.topic_id = t.id AND p.is_active = 1
-- GROUP BY t.id ORDER BY t.display_order;

-- Fetch all sample test cases for a problem (frontend-safe):
-- SELECT id, input_data, expected_output, explanation, time_limit_ms, memory_limit_kb, order_no
-- FROM test_cases WHERE problem_id = ? AND case_type = 'sample' AND is_active = 1
-- ORDER BY order_no;

-- Fetch ALL test cases for judge (server-side only):
-- SELECT * FROM test_cases WHERE problem_id = ? AND is_active = 1 ORDER BY order_no;
