package com.example.domain.repository

import com.example.domain.model.Booking
import com.example.domain.model.BookingStatus
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getBooking(bookingId: String): Flow<Booking?>
    fun getBookingsForGuest(guestId: String): Flow<List<Booking>>
    fun getBookingsForHost(hostId: String): Flow<List<Booking>>
    suspend fun saveBooking(booking: Booking)
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)
    suspend fun hostCheckIn(bookingId: String)
    suspend fun signDigitalWaiver(bookingId: String)
}
