-- Replaces the Module Evaluation and Placement Evaluation criteria rows
-- (moduleevaluationcriteria / placementevaluationcriteria, schema cranescrm)
-- with the updated marks policy. Wrapped in a transaction so it's all-or-nothing.
--
-- Run against the production RDS, e.g.:
--   mysql -h cranescrmdb.ccifwmepkgrk.us-east-2.rds.amazonaws.com -u crmuser -p cranescrm --skip-ssl < evaluation-policy-update.sql

START TRANSACTION;

DELETE FROM moduleevaluationcriteria;
INSERT INTO moduleevaluationcriteria (types, modes, marks, total, passmarks, updatedon, updatedby) VALUES
('MCQ (2 Nos, 20 Marks Each - Average)', 'Online Platform', '20', '20', '10', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Theory Exam', 'Pen & Paper', '25', '25', '12.5', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Lab Test', 'Practical', '25', '25', '12.5', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Assignments (Per Module)', '-', '10', '10', '5', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Module Total', '-', '-', '80', '40', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Capstone Project 1', 'Online / Offline', '25', '25', '12.5', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Capstone Project 2', 'Online / Offline', '25', '25', '12.5', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Specialization Project', 'Online / Offline', '50', '50', '25', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('PGDEA - Total Project Score (2 Capstone + 1 Specialization)', '-', '-', '100', '50', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Advance Diploma - Total Project Score (2 Capstone Only)', '-', '-', '50', '25', NOW(), 'vishnukant.reddy@cranesvarsity.com');

DELETE FROM placementevaluationcriteria;
INSERT INTO placementevaluationcriteria (types, testname, modes, marks, total, passmarks, updatedon, updatedby) VALUES
('Placement Test', 'PT1', 'Online, 40 Questions in 20 Minutes', '20', '20', '10', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Placement Test', 'PT2', 'Online, 40 Questions in 20 Minutes', '20', '20', '10', NOW(), 'vishnukant.reddy@cranesvarsity.com'),
('Placement Test', 'PT3', 'Online, 40 Questions in 20 Minutes', '20', '20', '10', NOW(), 'vishnukant.reddy@cranesvarsity.com');

COMMIT;
