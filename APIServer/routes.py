from fastapi import APIRouter
from db import table_connection, table_boards, table_mpg_gallon_acceleration_weight
from pymongo import DESCENDING
from fastapi.encoders import jsonable_encoder

router = APIRouter()
    
@router.get("/wemos/getConnection")
async def send_board_info_to_app():
    # Full response message: List of Json Objects
    message = []
    # Iterate over every board
    for board in table_boards.find():
        connection_status = table_connection.find_one({"id": board['id']})
        message_item = { "board_id": board['id'],
                     "board_name": board['name'],
                     "board_ip": board['ip'],
                     "status": connection_status['status'],
                     "timestamp": connection_status['timestamp'],}
        message.append(message_item)
    return message

@router.get("/ML/MPG")
async def response_predicion():
    # Extract the latest row/collection in the mpg_gallon collection
    response = table_mpg_gallon_acceleration_weight.find_one(sort=[('_id', DESCENDING)], limit=1)
    return { "mpg": response['mpg'], "gallon_gas": response["gallon"], "weight": response["weight"], "acceleration": response["acceleration"] }