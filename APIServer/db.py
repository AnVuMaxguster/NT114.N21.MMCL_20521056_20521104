from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017")

# Init database
db = client['Monitor']

# Init collections (tables)
table_connection = db['connection']
table_boards = db['boards']
table_mpg_gallon_acceleration_weight = db['mpg_gallon_acceleration_weight']