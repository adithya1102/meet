package com.example

object AppConstants {
    // Supabase
    const val SUPABASE_URL = "TODO_REPLACE_WITH_SUPABASE_URL"
    const val SUPABASE_ANON_KEY = "TODO_REPLACE_WITH_SUPABASE_ANON_KEY"
    
    // Firebase (also needs google-services.json — add placeholder comment)
    // TODO: Replace google-services.json in app/ directory with real file from Firebase Console
    
    // Razorpay
    const val RAZORPAY_KEY_ID = "TODO_REPLACE_WITH_RAZORPAY_KEY_ID"
    // Note: Razorpay Secret Key must NEVER be in the Android app — server-side only
    
    // Aadhaar KYC (Setu API)
    const val SETU_KYC_BASE_URL = "https://dg-sandbox.setu.co" // sandbox
    const val SETU_CLIENT_ID = "TODO_REPLACE_WITH_SETU_CLIENT_ID"
    const val SETU_CLIENT_SECRET = "TODO_REPLACE_WITH_SETU_CLIENT_SECRET"
    
    // WhatsApp Business API (via Interakt)
    const val INTERAKT_API_KEY = "TODO_REPLACE_WITH_INTERAKT_API_KEY"
    
    // Google OAuth Web Client ID (from Firebase Console > Authentication > Google)
    const val GOOGLE_OAUTH_WEB_CLIENT_ID = "TODO_REPLACE_WITH_WEB_CLIENT_ID"
    
    // Platform settings
    const val PLATFORM_FEE_PERCENT = 0.15 // 15%
    const val PAYMENT_TIMEOUT_MINUTES = 15L
    const val OVERSTAY_MULTIPLIER = 2.0 // 2x hourly rate for overstay penalty
    const val BOOKING_BUFFER_MINUTES = 30 // gap between bookings
}
