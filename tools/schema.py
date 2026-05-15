"""Policy 스키마 — Kotlin `data/model/Policy.kt` 미러.

빌드 단계에서 생성·보강한 정책 JSON을 클라이언트가 deserialize 할 수 있도록
필드 이름·타입을 1:1 매칭한다. 새 필드를 추가할 때는 Kotlin 측이 무시해도
안전한 옵션 필드로만 추가할 것 (`ignoreUnknownKeys=true` 설정돼 있음).
"""

from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field


class DocumentRequirement(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str
    sourceUrl: Optional[str] = None


class EligibilityRule(BaseModel):
    model_config = ConfigDict(extra="forbid")

    minAge: Optional[int] = None
    maxAge: Optional[int] = None
    regions: Optional[List[str]] = None
    requiresOccupation: Optional[List[str]] = None
    requiresMarried: Optional[bool] = None
    requiresChildren: Optional[bool] = None


class Policy(BaseModel):
    # Kotlin 측 unknown field 무시 정책에 맞춰 extra=allow.
    # (Gemini가 실수로 새 필드 끼워도 빌드를 깨지 않게)
    model_config = ConfigDict(extra="allow")

    id: str
    title: str
    amount: int
    deadline: Optional[str] = None
    daysLeft: Optional[int] = None
    category: str
    summary: str
    period: Optional[str] = None
    eligibility: List[str] = Field(default_factory=list)
    documents: List[DocumentRequirement] = Field(default_factory=list)
    procedure: List[str] = Field(default_factory=list)
    applicationOrg: Optional[str] = None
    applicationUrl: Optional[str] = None
    eligibilityRule: Optional[EligibilityRule] = None

    # 빌드 단계에서만 채우는 보강 필드 (클라이언트는 unknown으로 무시)
    difficultyScore: Optional[int] = None  # 1~10, 낮을수록 쉬움
    roiScore: Optional[int] = None  # 0~100, 높을수록 가성비
