# SimilarityFinder

This application can be used to detect similarities among articles published in the media (e.g. 
newspaper articles). In order to detect such similarities, frequency vectors of n-grams (more 
specifically trigrams) are compared with each other using the concept of cosine similarity.

The UI of this application is based on JavaFX and allows to configure which articles that are stored
in the used database should be compared with each other. Two articles are considered similar if 
their content's cosine similarity index is greater or equal to the configurable threshold value 
or if there exists another article that is similar to both of them.
