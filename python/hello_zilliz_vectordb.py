import configparser
import time
import random
from pymilvus import connections, utility
from pymilvus import Collection, DataType, FieldSchema, CollectionSchema

if __name__ == '__main__':
    # connect to milvus
    cfp = configparser.ConfigParser()
    cfp.read('config.ini')
    milvus_uri = cfp.get('example', 'uri')
    user = cfp.get('example', 'user')
    password = cfp.get('example', 'password')

    print("begin connect....")
    connections.connect("default",
                        uri=milvus_uri,
                        user=user,
                        password=password,
                        secure=True)
    print(f"Connecting to DB: {milvus_uri}")

    # Check if the collection exists
    collection_name = "book3"
    # check_collection = utility.has_collection(collection_name)
    # if check_collection:
    #     drop_result = utility.drop_collection(collection_name)
    # print("Success!")
    # create a collection with customized primary field: book_id_field
    dim = 128
    book_id_field = FieldSchema(name="book_id", dtype=DataType.INT64, is_primary=True,
                                description="customized primary id")
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
    nb = 10000
    insert_rounds = 500
    start = 0  # first primary key id
    total_rt = 0  # total response time for inert
    print(f"Inserting {nb * insert_rounds} entities... ")
    for i in range(insert_rounds):
        book_ids = [i for i in range(start, start + nb)]
        word_counts = [random.randint(1, 100) for i in range(nb)]
        book_intros = [[random.random() for _ in range(dim)] for _ in range(nb)]
        entities = [book_ids, word_counts, book_intros]
        t0 = time.time()
        ins_resp = collection.insert(entities)
        print(f"ins_resp:{ins_resp}")
        time.sleep(1)
        ins_rt = time.time() - t0
        start += nb
        total_rt += ins_rt
    print(f"Succeed in {round(total_rt, 4)} seconds!")
    # collection.load(timeout=300)
    # print(f"collection {collection_name} entities: {collection.num_entities}")
    # collection = Collection(name=collection_name)
    # resp = collection.load()
    # print(f"resp:{resp}")
    # print("123")
    connections.disconnect("default")
