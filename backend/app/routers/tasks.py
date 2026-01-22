from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import List, Optional
from ..database import get_db
from ..models import Task, User, Segment, Artwork, TaskStatus, UserRole
import random

router = APIRouter()

class TaskSubmission(BaseModel):
    submission_url: str
    telemetry_data: dict

@router.get("/assignments")
def get_assignments(user_id: int, db: Session = Depends(get_db)):
    # Simple fairness routing logic
    # Find pending tasks that match user's skills

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
         raise HTTPException(status_code=404, detail="User not found")

    # Team Leader View: See all submitted tasks
    if user.role == UserRole.TEAM_LEADER.value:
         # Return all submitted or pending review tasks
         tasks = db.query(Task).filter(Task.status.in_([TaskStatus.SUBMITTED.value, TaskStatus.APPROVED.value, TaskStatus.REJECTED.value])).all()
         result = []
         for t in tasks:
            segment = db.query(Segment).filter(Segment.id == t.segment_id).first()
            artwork = db.query(Artwork).filter(Artwork.id == segment.artwork_id).first()
            result.append({
                "task_id": t.id,
                "artwork_title": artwork.title,
                "segment_index": segment.segment_index,
                "master_image_url": artwork.master_image_url,
                "x": segment.x_offset,
                "y": segment.y_offset,
                "width": segment.width,
                "height": segment.height,
                "status": t.status
            })
         return result

    # Artist View
    # Find unassigned segments
    # In a real system, we'd do a complex query here
    tasks = db.query(Task).filter(Task.status == TaskStatus.PENDING.value).all()

    # Filter based on "Fairness Score" = skill_match * trust_score * equity_weight
    # Mocking this selection for prototype

    assigned_tasks = []

    # If user has no tasks, assign one if available
    user_tasks = db.query(Task).filter(Task.assignee_id == user_id, Task.status == TaskStatus.ASSIGNED.value).all()

    if not user_tasks and tasks:
        # Assign the first available task
        task = tasks[0]
        task.assignee_id = user_id
        task.status = TaskStatus.ASSIGNED.value
        db.commit()
        db.refresh(task)
        user_tasks = [task]

    result = []
    for t in user_tasks:
        segment = db.query(Segment).filter(Segment.id == t.segment_id).first()
        artwork = db.query(Artwork).filter(Artwork.id == segment.artwork_id).first()
        result.append({
            "task_id": t.id,
            "artwork_title": artwork.title,
            "segment_index": segment.segment_index,
            "master_image_url": artwork.master_image_url,
            "x": segment.x_offset,
            "y": segment.y_offset,
            "width": segment.width,
            "height": segment.height,
            "status": t.status
        })

    return result

@router.post("/submit/{task_id}")
def submit_task(task_id: int, submission: TaskSubmission, db: Session = Depends(get_db)):
    task = db.query(Task).filter(Task.id == task_id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")

    task.submission_url = submission.submission_url
    task.telemetry_data = submission.telemetry_data
    task.status = TaskStatus.SUBMITTED.value

    # Calculate Human Confidence Score (Mock)
    # 0-100 score based on telemetry presence
    if submission.telemetry_data:
        task.human_confidence_score = random.uniform(80, 100)
    else:
        task.human_confidence_score = 0

    # Calculate Ownership Score
    # (StrokeCount * Weight) + Area
    # Mocking stroke count from telemetry size
    stroke_count = len(submission.telemetry_data.get("stroke_timestamps", []))
    area = 100 * 100 # Mock area
    task.stroke_count = stroke_count
    task.ownership_score = (stroke_count * 0.5) + area

    db.commit()
    return {"status": "submitted", "human_confidence": task.human_confidence_score}
