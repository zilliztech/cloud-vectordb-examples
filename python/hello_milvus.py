import configparser
import random
from pymilvus import (
    connections,
    utility,
    FieldSchema,
    CollectionSchema,
    DataType,
    Collection,
)




if __name__ == '__main__':

    cfp = configparser.ConfigParser()
    cfp.read('config.ini')
    milvus_host = cfp.get('example', 'endpoint')
    milvus_port = cfp.get('example','port')
    username = cfp.get('example', 'username')
    password = cfp.get('example', 'password')

    """
    step: connect milvus
    parameters: host, port, user, password, secure=true
    """
    connections.connect("default",
                        host=milvus_host,
                        port=milvus_port,
                        user=username,
                        password=password,
                        secure=True)

    """
    step: check collection is existed
    parameters: collection_name
    return: Boolean
    """
    check_collection = utility.has_collection("book")
    if check_collection:
        drop_result = utility.drop_collection("book")

    """
    step: create collection
    parameters: collection_schema, collection_name, using, shards_num, consistency_level
    """
    book_id = FieldSchema(
        name="book_id",
        dtype=DataType.INT64,
        is_primary=True,
    )
    word_count = FieldSchema(
        name="word_count",
        dtype=DataType.INT64,
    )
    book_intro = FieldSchema(
        name="book_intro",
        dtype=DataType.FLOAT_VECTOR,
        dim=2
    )
    schema = CollectionSchema(
        fields=[book_id, word_count, book_intro],
        description="Test book search"
    )
    collection = Collection(
        name="book",
        schema=schema,
        using='default',
        shards_num=2,
        consistency_level="Strong"
    )
    print("collection schema:", collection.schema)
    print("collection description:", collection.description)
    print("collection name:", collection.name)
    print("collection is_empty:", collection.is_empty)
    print("collection num_entities:", collection.num_entities)
    print("collection primary_field:", collection.primary_field)

    """
    step: create partition
    parameters: partition_name
    """
    partition = collection.create_partition("book_part")
    print("partition name:", partition.name)
    print("partition description:", partition.description)
    print("partition is empty:", partition.is_empty)
    print("partition num entities:", partition.num_entities)

    """
    step: insert entities
    parameters: data
    return: MutationResult
    """
    data = [
        [i for i in range(2000)],
        [i for i in range(10000, 12000)],
        [[random.random() for _ in range(2)] for _ in range(2000)],
    ]
    insert_result = collection.insert(data)
    print("insert count: ", insert_result.insert_count)

    """
    step: create index
    parameters: metric_type, index_type, params, field_name
    """
    index_params = {
        "metric_type": "L2",
        "index_type": "IVF_FLAT",
        "params": {"nlist": 1024}
    }
    index_result = collection.create_index(
        field_name="book_intro",
        index_params=index_params
    )
    print("create index: ", index_result)

    """
    step: load collection
    desc: All search and query operations within Milvus are executed in memory. 
          Load the collection to memory before conducting a vector search.
    """
    load_result = collection.load()

    """
    step: conduct a hybrid search
    parameters: data, anns_field, metric_type, params, limit, expression
    return: SearchResult
    """
    search_param = {
        "data": [[0.1, 0.2]],
        "anns_field": "book_intro",
        "param": {"metric_type": "L2", "params": {"nprobe": 10}},
        "limit": 2,
        "expr": "word_count <= 11000",
    }
    search_result = collection.search(**search_param)
    print("search result:", search_result)

    """
    step: conduct a vector query
    parameters: expression, output_fields, consistency_level
    return: list of result
    """
    query_result = collection.query(
        expr="book_id in [2,4,6,8]",
        output_fields=["book_id", "book_intro"],
        consistency_level="Strong"
    )
    print("query count: ", len(query_result))
    for x in query_result:
        print("query result: book_id", x["book_id"], "book_intro", x["book_intro"], end='\n')

    """
    step: delete entities
    parameters: expression 
    result: MutationResult
    """
    expr = "book_id in [0,1]"
    delete_result = collection.delete(expr)
    print("delete count: ", delete_result.delete_count)

    """
    step: release collection
    """
    release_result = collection.release()

    """
    step: drop collection
    """
    drop_result = utility.drop_collection("book")

    """
    step: close connect
    """
    connections.disconnect("default")

