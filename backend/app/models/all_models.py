from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey, DateTime, JSON, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import enum
from ..database import Base

class UserRole(enum.Enum):
    ARTIST = "artist"
    TEAM_LEADER = "team_leader"
    ADMIN = "admin"

class TaskStatus(enum.Enum):
    PENDING = "pending"
    ASSIGNED = "assigned"
    SUBMITTED = "submitted"
    APPROVED = "approved"
    REJECTED = "rejected"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)
    full_name = Column(String)
    role = Column(String, default=UserRole.ARTIST.value)

    # Location Hierarchy
    village_id = Column(Integer, ForeignKey("villages.id"))

    # Skill & Trust
    skill_badges = Column(JSON, default={}) # {"kachni": true, "bharni": false}
    trust_score = Column(Float, default=1.0)

    tasks = relationship("Task", back_populates="assignee")
    village = relationship("Village", back_populates="users")

class Village(Base):
    __tablename__ = "villages"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    block = Column(String)
    district = Column(String)
    state = Column(String)
    boundary_geojson = Column(JSON) # GeoJSON Polygon

    users = relationship("User", back_populates="village")

class Artwork(Base):
    __tablename__ = "artworks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String)
    master_image_url = Column(String)
    total_segments = Column(Integer)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    segments = relationship("Segment", back_populates="artwork")

class Segment(Base):
    __tablename__ = "segments"

    id = Column(Integer, primary_key=True, index=True)
    artwork_id = Column(Integer, ForeignKey("artworks.id"))
    segment_index = Column(Integer)
    # Coordinates for the tile in the master image
    x_offset = Column(Integer)
    y_offset = Column(Integer)
    width = Column(Integer)
    height = Column(Integer)

    artwork = relationship("Artwork", back_populates="segments")
    task = relationship("Task", back_populates="segment", uselist=False)

class Task(Base):
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    segment_id = Column(Integer, ForeignKey("segments.id"))
    assignee_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    status = Column(String, default=TaskStatus.PENDING.value)

    # Telemetry & Validation
    telemetry_data = Column(JSON, nullable=True)
    human_confidence_score = Column(Float, nullable=True)
    fairness_score_snapshot = Column(Float, nullable=True)

    # Submission
    submission_url = Column(String, nullable=True) # SVG/Image URL
    submitted_at = Column(DateTime(timezone=True), nullable=True)

    # Attribution
    stroke_count = Column(Integer, default=0)
    ownership_score = Column(Float, default=0.0)

    segment = relationship("Segment", back_populates="task")
    assignee = relationship("User", back_populates="tasks")
