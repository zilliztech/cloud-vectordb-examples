## Getting started
The example shows how to connect to Zilliz Cloud, create a collection with customized primary field, build
index, load collection and query a vector to get the most similar entities.
### Prerequisites
    Install Python 3.7+
    pip3


### Clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples.git

### Install pymilvus
    pip3 install pymilvus==2.2.11

### Go to python folder
    cd cloud-vectordb-examples
    cd python

### Update uri,token or username and password in config.ini
    uri = https://in01-XXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    user = db_admin
    password = ********
    token = ********

### Run hello_zilliz.py
    python3 hello_zilliz.py

### It should print information on the console
```text
Connecting to Zilliz Cluster: https://in03-f84b6f2ac748efc.api.gcp-us-west1.zillizcloud.com
Success!

Creating example collection book
Schema: {'auto_id': False, 'description': 'my first collection', 'fields': [{'name': 'book_id', 'description': 'customized primary id', 'type': <DataType.INT64: 5>, 'is_primary': True, 'auto_id': False}, {'name': 'word_count', 'description': 'word count', 'type': <DataType.INT64: 5>}, {'name': 'book_intro', 'description': '', 'type': <DataType.FLOAT_VECTOR: 101>, 'params': {'dim': 64}}]}
Success!

Inserting 1000 * 2 entities...
Succeed in 1.3933 seconds!

Flushing...
Succeed in 3.6033 seconds!

Building AutoIndex...
Succeed in 4.3388 seconds!

Loading collection...
Succeed in 5.0596 seconds!

Searching 1th vector: [[0.6553931903562082, 0.3523038174256873, 0.44569621648550495, 0.43901932947372335, 0.22057148194026943, 0.5873114356126942, 0.7924758626316987, 0.38361401831734643, 0.6664773009624367, 0.12340462542277142, 0.2743029091939969, 0.9162156232449277, 0.5009154562181873, 0.7193854064719712, 0.495909512647582, 0.990337288436552, 0.6058139013821308, 0.5507738533960046, 0.16588357608378024, 0.16916314989477754, 0.9348780413553427, 0.8320883823134162, 0.8996209055874194, 0.2167117665405459, 0.47684609120531296, 0.61400108394542, 0.25297242387092933, 0.4467253209287888, 0.6967931461024045, 0.9328427824632523, 0.5765219336520203, 0.2089800954473412, 0.7374114429154053, 0.9167776832808886, 0.6520457371133284, 0.24090824673857192, 0.12017458381932367, 0.77543758516493, 0.622821580977268, 0.16983574056674722, 0.24337003592222273, 0.09170286474657663, 0.6139047384939057, 0.2752582492147262, 0.6276882737152032, 0.7498826597231223, 0.8238220085440174, 0.3249944996890718, 0.6295958343124256, 0.11881702620285273, 0.43160038092405184, 0.6009382686020128, 0.06652421415982235, 0.41603888794615185, 0.6970843857651777, 0.3217636623556801, 0.8232692108399302, 0.8232604145211451, 0.9714272646891686, 0.7650669820483371, 0.4396265029945732, 0.48271714401339083, 0.19879585525075183, 0.8355980549702023]]

Result: ["['id: 930, distance: 5.743136405944824, entity: {}']"]
latency: 0.3067 seconds!
```

    
