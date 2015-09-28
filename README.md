# FilterHash
An open addressing based hash table implementation

Terminology
We are hashing n elements into k locations. The load factor is α=n/k.

Description of the algorithm
The idea is to partition the set of indexes (i.e. the underlying array) into i subsets. Notice that the partition is purely conceptual: 
In practice there is only one array, but our algorithm will operate on the array as if it was split into i subarrays.
Order the subsets by descending size. Each of these subsets will be mapped by a hash table with the following closed addressing collision strategy: 
Rehash the colliding item into the next hash table in the ordered sequence of hash tables. Thus, the first (and biggest) hash table will receive all of the n items we are hashing. 
And the expected number of collisions in one hash table will be the input for the next hash table.
We can achieve any desired load factor α=n/k in a given hash table, by choosing k small enough relative to n. 
And if all i hash tables have an expected load factor of α, the whole data structure as such will have a load factor of α.

