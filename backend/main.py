from fastapi import FastAPI
from pydantic import BaseModel
from typing import List

app = FastAPI()

class Task(BaseModel):
    id: int
    title: str
    status: str = "pending"
    segment_index: int = 1

@app.get("/tasks/assignments")
async def get_assignments():
    # This matches the data you inserted into Docker
    return [
        {"id": 1, "title": "Madhubani Masterpiece", "status": "pending", "segment_index": 1},
        {"id": 5, "title": "Madhubani Masterpiece", "status": "pending", "segment_index": 2}
    ]

# Run this using: pip install fastapi uvicorn && uvicorn main:app --reload --port 8000
