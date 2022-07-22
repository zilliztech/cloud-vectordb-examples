import configparser
import time
import random
from pymilvus import connections, utility
from pymilvus import Collection, DataType, FieldSchema, CollectionSchema


if __name__ == '__main__':
    # connect to milvus
    cfp = configparser.ConfigParser()
    cfp.read('config.ini')
    milvus_host = cfp.get('example', 'uri')
    milvus_port = cfp.get('example', 'port')
    user = cfp.get('example', 'user')
    password = cfp.get('example', 'password')

    connections.connect("default",
                        host=milvus_host,
                        port=milvus_port,
                        user=user,
                        password=password,
                        secure=True)
    print(f"start to connect to {milvus_host}")

    # Check if the collection exists
    collection_name = "book"
    check_collection = utility.has_collection(collection_name)
    if check_collection:
        drop_result = utility.drop_collection(collection_name)

    # create a collection with customized primary field: book_id_field
    dim = 128
    book_id_field = FieldSchema(name="book_id", dtype=DataType.INT64, is_primary=True, description="customized primary id")
    word_count_field = FieldSchema(name="word_count", dtype=DataType.INT64, description="word count")
    book_intro_field = FieldSchema(name="book_intro", dtype=DataType.FLOAT_VECTOR, dim=dim)
    schema = CollectionSchema(fields=[book_id_field, word_count_field, book_intro_field],
                          auto_id=False,
                          description="my first collection")
    collection = Collection(name=collection_name, schema=schema)
    print(f"create collection {collection_name} successfully")

    # insert data with customized ids
    nb = 10000
    insert_rounds = 10
    start = 0           # first primary key id
    total_rt = 0        # total response time for inert
    for i in range(10):
        book_ids = [i for i in range(start, start+nb)]
        word_counts = [random.randint(1, 100) for i in range(nb)]
        book_intros = [[random.random() for _ in range(dim)] for _ in range(nb)]
        entities = [book_ids, word_counts, book_intros]
        t0 = time.time()
        ins_resp = collection.insert(entities)
        ins_rt = time.time() - t0
        start += nb
        total_rt += ins_rt
    print(f"totally insert {nb * insert_rounds} entities cost {round(total_rt,4)} seconds")
    print(f"collection {collection_name} entities: {collection.num_entities}")

    # build index
    index_params = {"index_type": "HNSW", "metric_type": "L2", "params": {"M": 8, "efConstruction": 100}}
    t0 = time.time()
    collection.create_index(field_name=book_intro_field.name, index_params=index_params)
    t1 = time.time()
    print(f"collection {collection_name} build index in {round(t1-t0, 4)} seconds")

    # load collection
    t0 = time.time()
    collection.load()
    t1 = time.time()
    print(f"collection {collection_name} load in {round(t1-t0, 4)} seconds")

    # search
    nq = 1
    search_params = {"metric_type": "L2", "params": {"ef": 32}}
    topk = 1
    for i in range(10):
        search_vec = [[random.random() for _ in range(dim)] for _ in range(nq)]
        t0 = time.time()
        results = collection.search(search_vec,
                                anns_field=book_intro_field.name,
                                param=search_params,
                                limit=topk,
                                guarantee_timestamp=1)
        t1 = time.time()
        print(f"search {i} latency: {round(t1-t0, 4)} seconds")

    connections.disconnect("default")
    print("completed")

