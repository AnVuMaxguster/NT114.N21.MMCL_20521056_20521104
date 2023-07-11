import paho.mqtt.client as mqtt
from db import table_connection, table_boards, table_mpg_gallon_acceleration_weight
from pymongo import DESCENDING
import json
import threading
import signal
import time

# ML related
import pickle
import numpy as np
import pandas as pd

# Read the CSV file
df = pd.read_csv('auto-mpg.csv')

# Extract unique values from a specific column into an array
column_values = df['acceleration'].unique()

exit_event = threading.Event()

def mqtt_thread():
    # MQTT broker details
    broker = '192.168.1.11'
    port = 1883
    # Topics
    topic_wemos_connection = '/wemos/connection'
    topic_ML_input = '/ML/input'
    # Load the ML model
    model = pickle.load(open('mpg_prediction.pkl', 'rb')) 

    # Callback function when a connection is established
    def on_connect(client, userdata, flags, rc):
        # Notify successful connection
        print("Connected !")
        # Subscribe to the topics
        client.subscribe(topic_wemos_connection)
        client.subscribe(topic_ML_input)


    # Callback function when a message is received ( messages are queued )
    def on_message(client, userdata, msg):
        # Get current timestamp
        timestamp = int(time.time())
        # Deserialize received Json message
        payload = msg.payload.decode("utf-8")
        data = json.loads(payload)
        print(data)
        # Checking the topic to perform the corresponding task
        if msg.topic == topic_wemos_connection:
            # Condition: Check match id
            condition = { "id": data["id"] } 
            # Update the connection data of the board with the same id.
            doc_connection = { "id": data["id"], "status": data["status"], "timestamp": timestamp } # Create a row/document
             # Store board status into the corresponding collection of the local database
            table_connection.insert_one(doc_connection) 
            doc_boards = { "id": data["id"],
                            "name": data["name"],
                            "ip": data["ip"] } # Create a row/document
            # Store the board info into the corresponding collection of the local database
            table_boards.replace_one(condition, doc_boards, upsert=True) #upsert =True: add the document if the condition is not met.
        elif msg.topic == topic_ML_input:
            mpg = 0
            acceleration = 0
            if data["acceleration"] == -1: # Initial suggested mpg
                print("Init suggested msg.")
                for value in column_values:
                    ML_input_data = np.array([[data["cylinders"], data["displacement"], data["horsepower"],  
                                        data["weight"], value, data["model_year"], data["origin"]]])
                    temp_mpg = float(model.predict(ML_input_data))
                    if temp_mpg > mpg:
                        mpg = temp_mpg
                        acceleration = value
            else:  # Mpg preduction throughout the trip
                acceleration = data["acceleration"]
                ML_input_data = np.array([[data["cylinders"], data["displacement"], data["horsepower"],  
                                        data["weight"], acceleration, data["model_year"], data["origin"]]])
                mpg = float(model.predict(ML_input_data)) # Predict the mpsg using the input data.


            gallon = data["gas"] # Extracting the current level of fuel.
            # Store mpg, gallon & acceleration into the corresponding collection of the local database
            doc_mpg_gallon = { "mpg": mpg, "gallon": gallon, "acceleration": acceleration, "weight": data["weight"], "timestamp": timestamp }
            table_mpg_gallon_acceleration_weight.insert_one(doc_mpg_gallon)
            
    # Create an MQTT client instance
    client = mqtt.Client()

    # Set the callback functions
    client.on_connect = on_connect  # Successful connect to broker
    client.on_message = on_message # Message listener ( from subcribed topic )

    # Connect to the broker
    client.connect(broker, port, 60)

    # Start the loop ( maintain the connection & the message listener )
    client.loop_start()

    if exit_event.is_set():
        # Disconnect the MQTT client
        client.disconnect()
        # Stop the MQTT client loop
        client.loop_stop()

def signal_handler(signum, frame):
    exit_event.set()

signal.signal(signal.SIGINT, signal_handler)
th = threading.Thread(target=mqtt_thread)
th.daemon = True
th.start()