import {
  MilvusClient,
  DataType,
  ConsistencyLevelEnum,
} from "@zilliz/milvus2-sdk-node";
import { config } from "./config.js";

const { uri, user, password } = config;

// connecting
console.info(`Connecting to DB: ${uri}`);
const client = new MilvusClient({ address: uri, username: user, password: password });
console.info(`Success!`);

(async () => {
  // dimension
  const dimension = 64;
  const collection_name = "hello_milvus";
  const num_of_rows = 1000;
  // create colleciton
  console.time(`Creating example collection: ${collection_name}`);
  await client.createCollection({
    collection_name,
    dimension,
  });
  console.timeEnd(`Creating example collection: ${collection_name}`);

  const data = [];
  Array(num_of_rows)
    .fill(1)
    .forEach(() => {
      data.push({
        id: Math.floor(Math.random() * 100000),
        vector: [...Array(dimension)].map(() => Math.random()),
      });
    });
  // inserting
  console.time(`Inserting 1000 entities successfully`);
  await client.insert({
    collection_name,
    data,
  });
  console.timeEnd(`Inserting 1000 entities successfully`);

  // search
  console.time(`Searching vector`);
  const res = await client.search({
    collection_name,
    vector: data[0]["vector"],
    output_fields: ["id"],
    vector_type: DataType.FloatVector,
    limit: 10,
    consistency_level: ConsistencyLevelEnum.Bounded,
  });
  console.timeEnd(`Searching vector`);
  console.log(res);
})();
