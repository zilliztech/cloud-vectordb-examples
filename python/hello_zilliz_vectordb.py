import configparser
import time
import random

from pymilvus import MilvusClient
from pymilvus import DataType

cfp = configparser.RawConfigParser()
cfp.read('config.ini')
milvus_uri = cfp.get('example', 'uri')
token = cfp.get('example', 'token')

milvus_client = MilvusClient(uri=milvus_uri, token=token)
print(f"Connected to DB: {milvus_uri} successfully")


# Check if the collection exists
collection_name = "book"
check_collection = milvus_client.has_collection(collection_name)

if check_collection:
    milvus_client.drop_collection(collection_name)
    print(f"Dropped the existing collection {collection_name} successfully")

dim = 64

print("Start to create the collection schema")
schema = milvus_client.create_schema()
schema.add_field("book_id", DataType.INT64, is_primary=True, description="customized primary id")
schema.add_field("word_count", DataType.INT64, description="word count")
schema.add_field("book_intro", DataType.FLOAT_VECTOR, dim=dim, description="book introduction")
print("Start to prepare index parameters with default AUTOINDEX")
index_params = milvus_client.prepare_index_params()
index_params.add_index("book_intro", metric_type="L2")

print(f"Start to create example collection: {collection_name}")
# create collection with the above schema and index parameters, and then load automatically
milvus_client.create_collection(collection_name, schema=schema, index_params=index_params)
collection_property = milvus_client.describe_collection(collection_name)
print("Collection details: %s" % collection_property)

# insert data with customized ids
nb = 1000
insert_rounds = 2
start = 0           # first primary key id
total_rt = 0        # total response time for inert

print(f"Start to insert {nb*insert_rounds} entities into example collection: {collection_name}")
for i in range(insert_rounds):
    vector = [random.random() for _ in range(dim)]
    rows = [{"book_id": i, "word_count": random.randint(1, 100), "book_intro": vector} for i in range(start, start+nb)]
    t0 = time.time()
    milvus_client.insert(collection_name, rows)
    ins_rt = time.time() - t0
    start += nb
    total_rt += ins_rt
print(f"Insert completed in {round(total_rt,4)} seconds")

print("Start to flush")
start_flush = time.time()
milvus_client.flush(collection_name)
end_flush = time.time()
print(f"Flush completed in {round(end_flush - start_flush, 4)} seconds")

# search
nq = 3
search_params = {"metric_type": "L2",  "params": {"level": 2}}
limit = 2

for i in range(5):
   search_vectors = [[random.random() for _ in range(dim)] for _ in range(nq)]
   t0 = time.time()
   results = milvus_client.search(collection_name,
                                  data=search_vectors,
                                  limit=limit,
                                  search_params=search_params,
                                  anns_field="book_intro")
   t1 = time.time()
   assert len(results) == nq
   assert len(results[0]) == limit
   print(f"Search {i} results: {results}")
   print(f"Search {i} latency: {round(t1-t0, 4)} seconds")

