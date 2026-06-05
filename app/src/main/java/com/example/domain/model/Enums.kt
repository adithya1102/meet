package com.example.domain.model

enum class UserRole {
    GUEST, HOST, ADMIN
}

enum class BookingPurpose {
    CHILLOUT, DINE_OUT, MOVIE_NIGHT, PARTY, BOARD_GAMES, STUDY_GROUP
}

enum class BookingStatus {
    PENDING_PAYMENT, CONFIRMED, ACTIVE, EXTENDED, COMPLETED, OVERSTAYED, DISPUTED, CANCELLED
}

enum class AccessType {
    PRIVATE_STAIRCASE, SHARED_WALKTHROUGH
}

enum class VerificationStatus {
    UNVERIFIED, PENDING_INSPECTION, VERIFIED, SUSPENDED
}
