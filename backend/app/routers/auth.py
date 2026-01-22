from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from pydantic import BaseModel
from ..database import get_db
from ..models import User, Village, UserRole
from shapely.geometry import shape, Point
from passlib.context import CryptContext
from typing import Optional

router = APIRouter()
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

class UserRegister(BaseModel):
    username: str
    password: str
    full_name: str
    village_id: int
    role: str = "artist"

class UserLogin(BaseModel):
    username: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str
    user_id: int
    role: str

class GeoValidateRequest(BaseModel):
    latitude: float
    longitude: float
    village_id: int

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

@router.post("/register", response_model=Token)
def register(user: UserRegister, db: Session = Depends(get_db)):
    db_user = db.query(User).filter(User.username == user.username).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")

    hashed_password = get_password_hash(user.password)
    new_user = User(
        username=user.username,
        hashed_password=hashed_password,
        full_name=user.full_name,
        village_id=user.village_id,
        role=user.role
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return {"access_token": new_user.username, "token_type": "bearer", "user_id": new_user.id, "role": new_user.role}

@router.post("/login", response_model=Token)
def login(user: UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(User).filter(User.username == user.username).first()
    if not db_user or not verify_password(user.password, db_user.hashed_password):
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    return {"access_token": db_user.username, "token_type": "bearer", "user_id": db_user.id, "role": db_user.role}

@router.post("/geo/validate")
def validate_geo(request: GeoValidateRequest, db: Session = Depends(get_db)):
    village = db.query(Village).filter(Village.id == request.village_id).first()
    if not village:
        raise HTTPException(status_code=404, detail="Village not found")

    if not village.boundary_geojson:
        # If no boundary is set, assume valid for prototype/testing
        return {"valid": True, "message": "No boundary set, validation skipped"}

    try:
        polygon = shape(village.boundary_geojson)
        point = Point(request.longitude, request.latitude)

        if polygon.contains(point):
             return {"valid": True, "message": "Location verified"}
        else:
             return {"valid": False, "message": "Location outside village boundary"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Geo validation error: {str(e)}")

@router.get("/registry/tree")
def get_registry_tree(db: Session = Depends(get_db)):
    villages = db.query(Village).all()
    # Build a simple tree structure for the frontend
    # State -> District -> Block -> Village
    tree = {}
    for v in villages:
        if v.state not in tree: tree[v.state] = {}
        if v.district not in tree[v.state]: tree[v.state][v.district] = {}
        if v.block not in tree[v.state][v.district]: tree[v.state][v.district][v.block] = []
        tree[v.state][v.district][v.block].append({"id": v.id, "name": v.name})
    return tree
