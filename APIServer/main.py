from fastapi import FastAPI
from routes import router
import mqtt # Start mqtt thread along with FastAPI

app = FastAPI()

app.include_router(router)

