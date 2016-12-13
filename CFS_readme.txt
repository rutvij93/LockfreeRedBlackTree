===========================================================================================================

+------------+
| TestRBTree |
+------------+
To check the performance of parallel insertions against sequential insertions

Command:
java TestRBTree <Name of the tree> <Number of Threads> <number of tasks>

E.g.
TestRBTree LockFreeRBTree 8 1000000
TestRBTree SeqRBTree 8 1000000

===========================================================================================================

+----------------------+
| TestLockFreeContains |
+----------------------+
To check the performance of parallel insertions and parallel contains operations against sequential ones

Command:
java TestLockFreeContains <Name of the tree> <Number of Threads> <number of tasks>

E.g.
TestLockFreeContains LockFreeRBTree 8 1000000
TestLockFreeContains SeqRBTree 8 1000000

===========================================================================================================

+---------+
| TestCFS |
+---------+
To check the implemetation of CFS.
CFS exhinits notions of priority, sleep fairness

Command:
java TestCFS <Name of the tree> <number of threads> <number of tasks>

E.g.
TestCFS SeqRBTree 8 50
TestCFS LockFreeRBTreeSeqDelete 8 50
TestCFS LockFreeRBTree 8 50	//Note: code deadlocks sometimes

===========================================================================================================

+--------------------+
| TestLockFreeRBTree |
+--------------------+
To check the implementation of red black tree

Command:

java TestLockFreeRBTree <number of threads> <number of tasks>

E.g. 
java TestLockFreeRBTree 4 4

// Note: code works with lower thread counts

===========================================================================================================
