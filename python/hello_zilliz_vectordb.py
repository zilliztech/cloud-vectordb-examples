import configparser
import time
import random
from pymilvus import connections, utility
from pymilvus import Collection, DataType, FieldSchema, CollectionSchema
import numpy as np

if __name__ == '__main__':
    # connect to milvus
    cfp = configparser.RawConfigParser()
    cfp.read('config.ini')
    milvus_uri = cfp.get('example', 'uri')
    user = cfp.get('example', 'user')
    password = cfp.get('example', 'password')
    connections.connect("default",
                        uri=milvus_uri,
                        user=user,
                        password=password)
    # # Please check your connection guide in Zilliz Cloud console, if the cluster provides a token, you can use it to authenticate your cluster
    # # token based cluster
    # token = cfp.get('example', 'token')
    # connections.connect("default",
    #                     uri=milvus_uri,
    #                     token=token)
    print(f"Connecting to DB: {milvus_uri}")

    # Check if the collection exists
    collection_name = "book"
    has_col_time=[]
    for i in range(50):
        start_has = time.time()*1000
        check_collection = utility.has_collection(collection_name)
        end_has = time.time()*1000
        print(f"has collection use {round(end_has - start_has, 2)} ms!")
        has_col_time.append(round(end_has - start_has, 4))
        if check_collection:
            drop_result = utility.drop_collection(collection_name)
            print("Success!")
    n_has_col_time=np.array(has_col_time)
    print(f"has collection :TP99 {round(np.percentile(n_has_col_time, 99),2)} ms,"
          f"TP50 {round(np.percentile(n_has_col_time, 50),2)} ms,"
          f"AVG {round(np.mean(n_has_col_time),2)} ms")
    # create a collection with customized primary field: book_id_field
    dim = 768
    book_id_field = FieldSchema(name="book_id", dtype=DataType.INT64, is_primary=True, description="customized primary id")
    word_count_field = FieldSchema(name="word_count", dtype=DataType.INT64, description="word count")
    book_intro_field = FieldSchema(name="book_intro", dtype=DataType.FLOAT_VECTOR, dim=dim)
    schema = CollectionSchema(fields=[book_id_field, word_count_field, book_intro_field],
                          auto_id=False,
                          description="my first collection")
    print(f"Creating example collection: {collection_name}")
    collection = Collection(name=collection_name, schema=schema)
    print(f"Schema: {schema}")
    print("Success!")

    # insert data with customized ids
    nb = 1000
    insert_rounds = 10
    start = 0           # first primary key id
    total_rt = 0        # total response time for inert
    print(f"Inserting {nb * insert_rounds} entities... ")
    for i in range(insert_rounds):
        book_ids = [i for i in range(start, start+nb)]
        word_counts = [random.randint(1, 100) for i in range(nb)]
        book_intros = [[random.random() for _ in range(dim)] for _ in range(nb)]
        entities = [book_ids, word_counts, book_intros]
        t0 = time.time()
        ins_resp = collection.insert(entities)
        ins_rt = time.time() - t0
        start += nb
        total_rt += ins_rt
    print(f"Succeed in {round(total_rt,4)} seconds!")
    # print(f"collection {collection_name} entities: {collection.num_entities}")

    # flush
    print("Flushing...")
    start_flush = time.time()
    collection.flush()
    end_flush = time.time()
    print(f"Succeed in {round(end_flush - start_flush, 4)} seconds!")
    # build index
    index_params = {"index_type": "AUTOINDEX", "metric_type": "L2", "params": {}}
    t0 = time.time()
    print("Building AutoIndex...")
    collection.create_index(field_name=book_intro_field.name, index_params=index_params)
    t1 = time.time()
    print(f"Succeed in {round(t1-t0, 4)} seconds!")

    # load collection
    t0 = time.time()
    print("Loading collection...")
    collection.load()
    t1 = time.time()
    print(f"Succeed in {round(t1-t0, 4)} seconds!")

    # search
    nq = 1
    search_params = {"metric_type": "L2",  "params": {"level": 2}}
    topk = 1
    search_time=[]
    for i in range(50):
        search_vec = [[random.random() for _ in range(dim)] for _ in range(nq)]
        # print(f"Searching vector: {search_vec}")
        t0 = time.time()*1000
        results = collection.search(search_vec,
                                anns_field=book_intro_field.name,
                                param=search_params,
                                limit=topk,
                                guarantee_timestamp=1)
        t1 = time.time()*1000
        # print(f"Result:{results}")
        print(f"search {i} latency: {round(t1-t0, 2)} ms!")
        search_time.append(round(t1-t0, 2))
    n_search_time=np.array(search_time)
    print(f"has collection :TP99 {round(np.percentile(n_has_col_time, 99), 2)} ms,"
          f"TP50 {round(np.percentile(n_has_col_time, 50), 2)} ms,"
          f"AVG {round(np.mean(n_has_col_time), 2)} ms")
    print(f"search latency :TP99 {round(np.percentile(n_search_time, 99), 2)} ms,"
          f"TP50 {round(np.percentile(n_search_time, 50), 2)} ms,"
          f"AVG {round(np.mean(n_search_time), 2)} ms")
    connections.disconnect("default")

