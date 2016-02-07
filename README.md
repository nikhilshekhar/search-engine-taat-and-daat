# TAAT-DAAT
Term at a time and Document at a time

The following functions is implemented. The results should be written into a log file (more descriptions on this latter), and the corresponding output formats are also defined in the following.

**• getTopK K:** This returns the key dictionary terms that have the K largest postings lists. The result is expected to be an ordered string in the descending order of result postings, i.e., largest in the first position, and so on.
The output is formatted as follows (K=10 for an example)
FUNCTION: getTopK 10
Result: term1, term2, term3..., term10 (list the terms)

**• getPostings query_term:** Retrieve the postings list for the given query. Since we have N input query terms, this function is executed N times, and output the postings for each term from both two different ordered postings list. The corresponding posting list is be displayed in the following format:
FUNCTION: getPostings query_term
Ordered by doc IDs: 100, 200, 300... (list the document IDs ordered by increasing document IDs)
Ordered by TF: 300, 100, 200... (list the document IDs ordered by decreasing term frequencies)
Should display “term not found” if it is not in the index.

**• termAtATimeQueryAnd query_term1, ..., query_termN:** This emulates an evaluation of a multi-term Boolean AND query on the index with term-at-a-time query. Note here the number of query terms could be varied. The index ordered by decreasing term frequencies should be used in this query. Although Java has many very powerful methods that can do intersections of sets very efficiently, it's not being used here. The query terms are processed in the order in which they appear in the query. For example, you should process query_term1 first, then query_term2, and so on. In order to learn the essence of the term-at-a-time strategy, we have to compare every docID in one posting with every docID in the other while performing the intersection. 

A query optimization by re-ordering them by the increasing size of the postings is also implemented. For example, if the sizes of postings for query_term1, query_term2 and query_term3 are 30, 20 and 50. Then in this part, you will process them in the following order: query_term2, query_term1 and query_term3.

The output file, displays the number of documents found, number of comparisons are made during this query and how much time it takes. The document IDs should be sorted and listed.
For example:
FUNCTION: termAtATimeQueryAnd query_term1, ..., query_termN
xx documents are found 
yy comparisons are made
zz seconds are used
nn comparisons are made with optimization (optional bonus part)
Result: 100, 200, 300 ... (list the document IDs, re-ordered by docIDs)
Should display “terms not found” if it is not in the index.

**• termAtATimeQueryOr query_term1, ..., query_termN:** This emulates an evaluation of a multi-term Boolean OR query on the index with term-at-a-time query. The index ordered by decreasing term frequencies is used in this query. All other requirements are the same with termAtATimeQueryAnd. Output format is the same as above.

**• docAtATimeQueryAnd query_term1, ..., query_termN:** This emulates an evaluation of a multi-term Boolean AND query on the index with document-at-a-time query. The index ordered by increasing document IDs is used in this query.Output format is the same.

**• docAtATimeQueryOr query_term1, ..., query_termN:** This emulates an evaluation of a multi-term Boolean OR query on the index with document-at-a-time query. The index ordered by increasing document IDs should be used in this query. Output format is the same.
