import { MilvusClient, DataType } from "@zilliz/milvus2-sdk-node";
import { config } from "./config.js";

const { uri, user, password, secure } = config;

// connecting
console.info(`Connecting to DB: ${uri}`);
const client = new MilvusClient(uri, secure, user, password, secure);
console.info(`Success!`);

// define schema
const collection_name = `book`;
const dim = 128;
const schema = [
  {
    name: `book_id`,
    description: `customized primary id`,
    data_type: DataType.Int64,
    is_primary_key: true,
    autoID: false,
  },
  {
    name: `word_count`,
    description: `word count`,
    data_type: DataType.Int64,
  },
  {
    name: `book_intro`,
    description: `word count`,
    data_type: DataType.FloatVector,
    type_params: {
      dim: dim,
    },
  },
];

const test = async () => {
  // create colleciton
  console.time(`Creating example collection: ${collection_name}`);
  console.info(`Schema: `, schema);
  await client.createCollection({
    collection_name,
    description: `my first collection`,
    fields: schema,
  });

  console.timeEnd(`Creating example collection: ${collection_name}`);

  const fields_data = [];
  Array(1000)
    .fill(1)
    .forEach(() => {
      let r = {};
      schema.forEach((s) => {
        r = {
          book_id: Math.floor(Math.random() * 100000),
          word_count: Math.floor(Math.random() * 1000),
          book_intro: [...Array(dim)].map(() => Math.random()),
        };
      });
      fields_data.push(r);
    });
  // inserting
  console.time(`Inserting 100000 entities successfully`);
  await client.insert({
    collection_name,
    fields_data,
  });
  console.timeEnd(`Inserting 100000 entities successfully`);

  // create index
  console.time(`Create index successfully`);
  await client.createIndex({
    collection_name,
    field_name: "book_intro",
    index_name: "myindex",
    extra_params: {
      index_type: "AUTOINDEX",
      metric_type: "L2",
    },
  });
  console.timeEnd(`Create index successfully`);
  // load collection
  console.time(`Load Collection successfully`);
  await client.loadCollectionSync({
    collection_name,
  });
  console.timeEnd(`Load Collection successfully`);

  // search
  console.time(`Searching vector:`);
  const searchVector = [...Array(dim)].map(() => Math.random());
  const res = await client.search({
    collection_name,
    vectors: [searchVector],
    search_params: {
      anns_field: "book_intro",
      metric_type: "L2",
      params: JSON.stringify({ nprobe: 64 }),
      topk: 1,
    },
    output_fields: ['book_id', 'word_count'],
    vector_type: DataType.FloatVector,
  });
  console.timeEnd(`Searching vector:`);
  console.log(res);
};

test();
