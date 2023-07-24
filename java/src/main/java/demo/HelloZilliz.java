package demo;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;

import java.util.*;

/*
Please check your connection guide in Zilliz Cloud console, if the cluster provides a token, you can use it to
 authenticate your cluster; or you can use username and password to connect to the cluster.

 connections.connect("default",
                     uri=self.public_endpoint,
                     token=self.token)

 connections.connect("default",
                    uri=self.public_endpoint,
                    user=self.user,
                    password=self.password)
*/

public class HelloZilliz {
    static final String public_endpoint = "https://in01-XXXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX";
    static final String token = "********";
    static final String username = "db_admin";
    static final String password = "********";

    int dimensions = 64;
    String collectionName = "book";
    int entity_nums = 1000;
    int insertRounds = 2;
    int searchRounds = 1;

    FieldType bookIdField;
    FieldType wordCountField;
    FieldType bookIntroField;

    /*
    * Generate fields with customized ids and random content, you need to call an embedding model to get the
    * real vector value in actual life. e.g.:https://platform.openai.com/docs/api-reference/embeddings
     */
    protected List<InsertParam.Field> generateFields(Integer start_id, Integer end_id){
        List<Long> book_id_array = new ArrayList<>();
        List<Long> word_count_array = new ArrayList<>();
        List<List<Float>> book_intro_array = new ArrayList<>();

        for (long i = start_id; i < end_id; ++i) {
            book_id_array.add(i);
            word_count_array.add(i + 10000);

            List<Float> vector = new ArrayList<>();
            for (int k = 0; k < dimensions; ++k) {
                vector.add(new Random().nextFloat());
            }
            book_intro_array.add(vector);

        }
        List<InsertParam.Field> fields = new ArrayList<>();

        fields.add(new InsertParam.Field(bookIdField.getName(), book_id_array));
        fields.add(new InsertParam.Field(wordCountField.getName(), word_count_array));
        fields.add(new InsertParam.Field(bookIntroField.getName(), book_intro_array));

        return fields;
    }

    /*
    * Generate a random vector for search
     */
    protected List<List<Float>> generateSearchVectors(){
        List<Float> floatList = new ArrayList<>();
        for (int k = 0; k < dimensions; ++k) {
            floatList.add(new Random().nextFloat());
        }
        return Collections.singletonList(floatList);
    }

    /*
    The example shows how to
        1. connect to Zilliz Cloud
        2. create a collection with customized primary field
        3. build index
        4. load collection
        3. query a vector to get the most similar entities.
     */
    protected void runExample(){

        // connect to milvus with token
        final MilvusServiceClient myMilvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withUri(public_endpoint)
                        .withToken(token)
                        .build());
        /*
        * connect to milvus with user and password
        final MilvusServiceClient myMilvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withUri(public_endpoint)
                        .withAuthorization(username, password)
                        .build());
         */

        System.out.println("Connecting to Zilliz Cluster:" + public_endpoint);

        // check if the collection exists
        R<DescribeCollectionResponse> response = myMilvusClient.describeCollection(DescribeCollectionParam.newBuilder()
                .withCollectionName(collectionName).build());
        if (response.getData() != null) {
            myMilvusClient.dropCollection(DropCollectionParam.newBuilder().withCollectionName(collectionName).build());
        }

        System.out.println("Success!\n");

        // create a collection with customized primary field: book_id_field
        bookIdField = FieldType.newBuilder()
                .withName("book_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        wordCountField = FieldType.newBuilder()
                .withName("word_count")
                .withDataType(DataType.Int64)
                .build();
        bookIntroField = FieldType.newBuilder()
                .withName("book_intro")
                .withDataType(DataType.FloatVector)
                .withDimension(dimensions)
                .build();

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("my first collection")
                .withShardsNum(2)
                .addFieldType(bookIdField)
                .addFieldType(wordCountField)
                .addFieldType(bookIntroField)
                .build();

        System.out.println("Creating example collection: " + collectionName);

        myMilvusClient.createCollection(createCollectionParam);

        System.out.println("Schema: " + createCollectionParam);
        System.out.println("Success!\n");

        //insert 1000*2 data with customized ids
        System.out.println("Inserting " + entity_nums * insertRounds + " entities... ");

        long startInsertTime = System.currentTimeMillis();
        for(int i=0; i<insertRounds; i++){
            List<InsertParam.Field> fields = generateFields(i*entity_nums, (i+1)*entity_nums);
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();
            myMilvusClient.insert(insertParam);
        }

        long insertTime = System.currentTimeMillis() - startInsertTime;
        System.out.println("Succeed in " + insertTime + " seconds!\n");

        //flush the data into disk from memory to make the data available,
        // see: https://milvus.io/docs/v1.1.1/flush_python.md
        System.out.println("Flushing...");

        long startFlushTime = System.currentTimeMillis();
        myMilvusClient.flush(FlushParam.newBuilder()
                .withCollectionNames(Collections.singletonList(collectionName))
                .withSyncFlush(true)
                .withSyncFlushWaitingInterval(50L)
                .withSyncFlushWaitingTimeout(30L)
                .build());

        long FlushTime = System.currentTimeMillis() - startFlushTime;
        System.out.println("Succeed in " + FlushTime / 1000.00 + " seconds!\n");

        // build index
        System.out.println("Building AutoIndex...");

        final IndexType INDEX_TYPE = IndexType.AUTOINDEX;   // IndexType
        long startIndexTime = System.currentTimeMillis();
        myMilvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldName(bookIntroField.getName())
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withSyncMode(Boolean.TRUE)
                        .withSyncWaitingInterval(500L)
                        .withSyncWaitingTimeout(30L)
                        .build());

        long IndexTime = System.currentTimeMillis() - startIndexTime;
        System.out.println("Succeed in " + IndexTime / 1000.00 + " seconds!\n");

        // load collection
        System.out.println("Loading collection...");

        long startLoadTime = System.currentTimeMillis();
        myMilvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withSyncLoad(true)
                .withSyncLoadWaitingInterval(500L)
                .withSyncLoadWaitingTimeout(100L)
                .build());

        long LoadTime = System.currentTimeMillis() - startLoadTime;
        System.out.println("Succeed in " + LoadTime / 1000.00 + " seconds\n");

        // search a random vector
        final Integer SEARCH_K = 2;
        // search params
        final String SEARCH_PARAM = "{\"nprobe\":10}";
        List<String> search_output_fields = Arrays.asList("book_id", "word_count");

        for (int i = 0; i < searchRounds; i++) {
            List<List<Float>> vectorToSearch = generateSearchVectors();

            System.out.println("Searching vector: " + vectorToSearch);
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(MetricType.L2)
                    .withOutFields(search_output_fields)
                    .withTopK(SEARCH_K)
                    .withVectors(vectorToSearch)
                    .withVectorFieldName(bookIntroField.getName())
                    .withParams(SEARCH_PARAM)
                    .build();

            long startSearchTime = System.currentTimeMillis();
            R<SearchResults> search = myMilvusClient.search(searchParam);

            long SearchTime = System.currentTimeMillis() - startSearchTime;
            System.out.println("Result: " + search.getData().getResults().getFieldsDataList());
            System.out.println("latency: " + SearchTime / 1000.00 + " seconds\n");
        }

        myMilvusClient.close();
    }
    public static void main(String[] args) {
        HelloZilliz helloZilliz = new HelloZilliz();
        helloZilliz.runExample();
    }

}
