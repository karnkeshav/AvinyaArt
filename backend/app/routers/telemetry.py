from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import List, Dict, Any
from ..database import get_db
from ..models import User

router = APIRouter()

class TelemetryData(BaseModel):
    user_id: int
    pressure_samples: List[float]
    velocity_samples: List[float]
    jitter_samples: List[float]
    stroke_timestamps: List[float]

@router.post("/submit")
def submit_telemetry(data: TelemetryData, db: Session = Depends(get_db)):
    # Simple algorithm to determine skills based on telemetry
    # In a real system, this would use an ML model

    avg_pressure = sum(data.pressure_samples) / len(data.pressure_samples) if data.pressure_samples else 0
    avg_velocity = sum(data.velocity_samples) / len(data.velocity_samples) if data.velocity_samples else 0

    # "Kachni" requires fine lines (low pressure)
    # "Bharni" requires filling (high pressure, consistent velocity)
    # "Godna" requires precision (low jitter)

    badges = {}
    if avg_pressure < 0.5:
        badges["kachni"] = True
    else:
        badges["kachni"] = False

    if avg_velocity > 10.0:
         badges["bharni"] = True
    else:
         badges["bharni"] = False

    # Update user badges
    user = db.query(User).filter(User.id == data.user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    current_badges = user.skill_badges or {}
    current_badges.update(badges)
    user.skill_badges = current_badges
    db.commit()

    return {"status": "processed", "new_badges": badges}

@router.get("/skill/badges/{user_id}")
def get_badges(user_id: int, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user.skill_badges or {}
