from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import List
from ..database import get_db
from ..models import Artwork, Segment, Task, TaskStatus
import uuid
import math

router = APIRouter()

@router.post("/upload")
def upload_artwork(title: str, file: UploadFile = File(...), segments: int = 4, db: Session = Depends(get_db)):
    # In a real app, upload to S3 or similar. Here we just mock the URL.
    # We could save it to disk if we wanted to be more complete.

    file_id = str(uuid.uuid4())
    # Mock URL
    image_url = f"http://localhost:8000/static/{file_id}.png"

    artwork = Artwork(
        title=title,
        master_image_url=image_url,
        total_segments=segments
    )
    db.add(artwork)
    db.commit()
    db.refresh(artwork)

    # Segment logic (Mock)
    # Assume square image 1000x1000
    width = 1000
    height = 1000
    cols = int(math.sqrt(segments))
    rows = int(math.ceil(segments / cols))

    seg_width = width // cols
    seg_height = height // rows

    tasks = []

    for i in range(segments):
        row = i // cols
        col = i % cols

        segment = Segment(
            artwork_id=artwork.id,
            segment_index=i,
            x_offset=col * seg_width,
            y_offset=row * seg_height,
            width=seg_width,
            height=seg_height
        )
        db.add(segment)
        db.commit()
        db.refresh(segment)

        task = Task(
            segment_id=segment.id,
            status=TaskStatus.PENDING.value
        )
        tasks.append(task)

    db.add_all(tasks)
    db.commit()

    return {"artwork_id": artwork.id, "segments_created": segments}

@router.post("/merge/{project_id}")
def merge_artwork(project_id: int, db: Session = Depends(get_db)):
    artwork = db.query(Artwork).filter(Artwork.id == project_id).first()
    if not artwork:
        raise HTTPException(status_code=404, detail="Artwork not found")

    # Check if all tasks are approved
    # For now, just check if submitted
    # In real app, we would download all submission images and stitch them together using PIL

    return {"status": "merged", "export_url": f"http://localhost:8000/export/{project_id}.pdf"}

@router.get("/ownership/{project_id}")
def get_ownership(project_id: int, db: Session = Depends(get_db)):
    # Calculate % ownership for each artist involved

    segments = db.query(Segment).filter(Segment.artwork_id == project_id).all()
    total_score = 0
    artist_scores = {}

    for seg in segments:
        task = db.query(Task).filter(Task.segment_id == seg.id).first()
        if task and task.assignee_id:
            score = task.ownership_score
            total_score += score
            aid = task.assignee_id
            if aid not in artist_scores:
                artist_scores[aid] = 0
            artist_scores[aid] += score

    ownership_breakdown = []
    if total_score > 0:
        for aid, score in artist_scores.items():
            percent = (score / total_score) * 100
            ownership_breakdown.append({"user_id": aid, "percentage": percent})

    return {"artwork_id": project_id, "ownership": ownership_breakdown}
