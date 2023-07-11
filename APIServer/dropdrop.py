from db import db

# Get a list of all collection names in the database
# collection_names = db.list_collection_names()

# Drop each collection
# for collection_name in collection_names:
#     db[collection_name].drop()

db['mpg_gallon'].drop()