import configparser
import time
import random
from pymilvus import connections, utility
from pymilvus import Collection, DataType, FieldSchema, CollectionSchema

""" 
Please check your connection guide in Zilliz Cloud console, if the cluster provides a token, you can use it to
 authenticate your cluster or you can use username and password to connect to the cluster.

 connections.connect("default",
                     uri=self.public_endpoint,
                     token=self.token)      

 connections.connect("default",
                    uri=self.public_endpoint,
                    user=self.user,
                    password=self.password)          
"""


class HelloZilliz():
    def __init__(self):
        config = configparser.RawConfigParser()
        config.read('config.ini')
        self.public_endpoint = config['example']['uri']
        self.user = config['example']['user']
        self.password = config['example']['password']
        self.token = config['example']['token']

        self.collection_name = "book"
        self.dimensions = 64

        self.entity_nums = 1000
        self.insert_rounds = 2

        self.search_rounds = 1

    def generate_entities(self, start_id, end_id):
        """
        Generate entities with customized ids and random content, you need to call an embedding model to get the
         real vector value in actual life. e.g.:https://platform.openai.com/docs/api-reference/embeddings
        :param start_id:
        :param end_id:
        :return: entities
        """
        book_ids = [i for i in range(start_id, end_id)]
        word_counts = [random.randint(1, 100) for i in range(end_id - start_id)]
        book_intros = [[random.random() for _ in range(self.dimensions)] for _ in range(end_id - start_id)]

        return [book_ids, word_counts, book_intros]

    def generate_search_vectors(self):
        """
        Generate a random vector for search
        :return: vector
        """
        return [[random.random() for _ in range(self.dimensions)]]

    def run_example(self):
        """
        The example shows how to
        1. connect to Zilliz Cloud
        2. create a collection with customized primary field
        3. build index
        4. load collection
        3. query a vector to get the most similar entities.
        :return:
        """

        ####################################################################
        # connect to zilliz cloud, you can use token or username and password
        ####################################################################
        # connect with token
        connections.connect(db_name="default",
                            uri=self.public_endpoint,
                            token=self.token)
        """
        # connect with username and password
        connections.connect(db_name="default",
                            uri=self.public_endpoint,
                            user=self.user,
                            password=self.password)
        """

        print("Connecting to Zilliz Cluster:", self.public_endpoint)

        ####################################################################
        # create a collection with customized primary field: book_id_field
        ####################################################################
        # check if the collection exists
        collection_exist = utility.has_collection(self.collection_name)
        if collection_exist:
            utility.drop_collection(self.collection_name)

        print("Success!\n")

        book_id_field = FieldSchema(name="book_id", dtype=DataType.INT64, is_primary=True,
                                    description="customized primary id")
        word_count_field = FieldSchema(name="word_count", dtype=DataType.INT64, description="word count")
        book_intro_field = FieldSchema(name="book_intro", dtype=DataType.FLOAT_VECTOR, dim=self.dimensions)

        collection_schema = CollectionSchema(fields=[book_id_field, word_count_field, book_intro_field],
                                             auto_id=False,
                                             description="my first collection")

        print("Creating example collection", self.collection_name)

        my_collection = Collection(name=self.collection_name, schema=collection_schema)

        print("Schema:", collection_schema)
        print("Success!\n")

        ####################################################################
        # insert 1000*2 data with customized ids
        ####################################################################
        print("Inserting", self.entity_nums, "*", self.insert_rounds, "entities... ")

        start_insert = time.time()
        for i in range(self.insert_rounds):
            # generate entities with random values, you need to call an embedding model to get the real vector value
            entities = self.generate_entities(self.entity_nums * i, self.entity_nums * (i + 1))
            my_collection.insert(entities)

        insert_time = time.time() - start_insert
        print("Succeed in", round(insert_time, 4), "seconds!\n")

        ####################################################################
        # flush the data into disk from memory to make the data available,
        # see: https://milvus.io/docs/v1.1.1/flush_python.md
        ####################################################################
        print("Flushing...")

        start_flush = time.time()
        my_collection.flush()

        flush_time = time.time() - start_flush
        print("Succeed in", round(flush_time, 4), "seconds!\n")

        ####################################################################
        # build index
        ####################################################################
        index_params = {"index_type": "AUTOINDEX", "metric_type": "L2", "params": {}}
        print("Building AutoIndex...")

        start_build_index = time.time()
        my_collection.create_index(field_name=book_intro_field.name, index_params=index_params)

        index_time = time.time() - start_build_index
        print("Succeed in", round(index_time, 4), "seconds!\n")

        ####################################################################
        # load collection
        ####################################################################
        print("Loading collection...")

        start_load = time.time()
        my_collection.load()

        load_time = time.time() - start_load
        print(f"Succeed in", round(load_time, 4), "seconds!\n")

        ####################################################################
        # search a random vector from my_collection with limit=1
        ####################################################################
        search_params = {"metric_type": "L2", "params": {"level": 2}}

        for i in range(self.search_rounds):
            vector_to_search = self.generate_search_vectors()
            print(f"Searching { i + 1 }th vector:{vector_to_search}")

            start_search = time.time()
            results = my_collection.search(data=vector_to_search,
                                           anns_field=book_intro_field.name,
                                           param=search_params,
                                           limit=1)
            print("\nResult:", results)

            search_time = time.time() - start_search
            print("latency:", round(search_time, 4), "seconds!\n")

        connections.disconnect("default")


if __name__ == '__main__':
    HelloZilliz().run_example()
