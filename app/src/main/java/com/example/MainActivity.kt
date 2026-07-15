package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CoralRed
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.DeepSapphire
import com.example.ui.theme.SandSoft

// Hotel Data Model
data class Hotel(
    val id: Int,
    val name: String,
    val location: String,
    val price: Double,
    val rating: Double,
    val reviewsCount: Int,
    val imageRes: Int,
    val category: String,
    val description: String,
    val amenities: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>
)

// Booking Transaction Model
data class Booking(
    val id: String,
    val hotel: Hotel,
    val roomType: String,
    val checkInDate: String,
    val durationNights: Int,
    val totalPrice: Double,
    val status: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// Format number helper (Indonesian Rupiah style)
fun formatRupiah(value: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID"))
    return formatter.format(value).replace("Rp", "Rp ").substringBefore(",")
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current

    // Navigation and screen states
    var currentTab by remember { mutableStateOf("cari") }
    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var searchGroupQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("Semua") }

    // Mock Database State
    val hotelList = remember {
        listOf(
            Hotel(
                id = 1,
                name = "The Grand Royal Palace",
                location = "Menteng, Jakarta Pusat",
                price = 1850000.0,
                rating = 4.9,
                reviewsCount = 420,
                imageRes = R.drawable.img_hotel_luxury,
                category = "Populer",
                description = "Terletak di kawasan elite Menteng, hotel mewah ini menghadirkan perpaduan sempurna antara arsitektur kolonial megah dan pelayanan bintang lima modern. Nikmati kolam renang infinity outdoor, restoran fine dining berpemandangan kota, serta spa premium untuk menyegarkan pikiran Anda.",
                amenities = listOf(
                    "Kolam Renang" to Icons.Filled.Pool,
                    "Wi-Fi Gratis" to Icons.Filled.Wifi,
                    "Pusat Kebugaran" to Icons.Filled.FitnessCenter,
                    "Spa Premium" to Icons.Filled.Spa,
                    "Layanan Kamar" to Icons.Filled.RoomService
                )
            ),
            Hotel(
                id = 2,
                name = "Samudra Beachfront Resort",
                location = "Nusa Dua, Bali",
                price = 2450000.0,
                rating = 4.8,
                reviewsCount = 512,
                imageRes = R.drawable.img_hotel_beach,
                category = "Pantai",
                description = "Terletak langsung di pantai pasir putih Nusa Dua, resort tropis premium ini menawarkan akses langsung ke samudera biru, villa dengan kolam renang pribadi, bar pantai yang meriah, serta restoran hidangan laut segar yang ditangkap langsung hari ini.",
                amenities = listOf(
                    "Akses Pantai" to Icons.Filled.BeachAccess,
                    "Kolam Renang" to Icons.Filled.Pool,
                    "Wi-Fi Gratis" to Icons.Filled.Wifi,
                    "Bar Pantai" to Icons.Filled.LocalBar,
                    "AC Dingin" to Icons.Filled.AcUnit
                )
            ),
            Hotel(
                id = 3,
                name = "Svara Mountain Lodge",
                location = "Lembang, Bandung",
                price = 1250000.0,
                rating = 4.7,
                reviewsCount = 188,
                imageRes = R.drawable.img_hotel_mountain,
                category = "Gunung",
                description = "Rasakan ketenangan di lereng gunung berkabut Lembang. Lodge kayu bernuansa hangat ini dikelilingi hutan pinus yang rindang. Sempurna untuk liburan keluarga dengan perapian hangat, ruang bersantai terbuka, dan jacuzzi berpemandangan lembah spektakuler.",
                amenities = listOf(
                    "Jacuzzi Hangat" to Icons.Filled.HotTub,
                    "Perapian" to Icons.Filled.Fireplace,
                    "Wi-Fi Gratis" to Icons.Filled.Wifi,
                    "Area Outbound" to Icons.Filled.Terrain,
                    "Sarapan Gratis" to Icons.Filled.FreeBreakfast
                )
            ),
            Hotel(
                id = 4,
                name = "Citadines Urban Suites",
                location = "Sudirman, Jakarta Selatan",
                price = 950000.0,
                rating = 4.6,
                reviewsCount = 310,
                imageRes = R.drawable.img_hotel_luxury,
                category = "Kota",
                description = "Ideal bagi pelancong bisnis maupun rekreasi perkotaan. Terletak strategis di pusat SCBD Sudirman dengan kamar luas bersuasana minimalis modern, dapur mini lengkap, fasilitas gym 24 jam, serta akses langsung ke stasiun MRT terdekat.",
                amenities = listOf(
                    "Gym 24 Jam" to Icons.Filled.FitnessCenter,
                    "Wi-Fi Kecepatan Tinggi" to Icons.Filled.Wifi,
                    "Dapur Mini" to Icons.Filled.Kitchen,
                    "Akses MRT" to Icons.Filled.DirectionsTransit,
                    "Ruang Rapat" to Icons.Filled.Business
                )
            ),
            Hotel(
                id = 5,
                name = "Ubud Sanctuary Villa",
                location = "Ubud, Gianyar",
                price = 2100000.0,
                rating = 4.9,
                reviewsCount = 289,
                imageRes = R.drawable.img_hotel_beach,
                category = "Resort",
                description = "Tersembunyi di kedalaman alam asri Ubud yang damai. Villa mewah ini menawarkan privasi absolut dengan desain bambu ramah lingkungan, pemandangan sawah terasering yang hijau, paviliun yoga terbuka, serta sesi meditasi terpandu setiap pagi.",
                amenities = listOf(
                    "Paviliun Yoga" to Icons.Filled.SelfImprovement,
                    "Kolam Pribadi" to Icons.Filled.Pool,
                    "Wi-Fi Gratis" to Icons.Filled.Wifi,
                    "Restoran Organik" to Icons.Filled.Restaurant,
                    "Layanan Spa" to Icons.Filled.Spa
                )
            ),
            Hotel(
                id = 6,
                name = "Giri Pine Forest Chalet",
                location = "Batu, Malang",
                price = 1100000.0,
                rating = 4.5,
                reviewsCount = 145,
                imageRes = R.drawable.img_hotel_mountain,
                category = "Gunung",
                description = "Chalet pegunungan modern yang menawan di lereng Gunung Banyak, Batu. Dilengkapi dengan balkoni panoramic yang menghadap ke gemerlap lampu kota Malang di malam hari, area api unggun komunal, dan udara pegunungan yang sangat bersih.",
                amenities = listOf(
                    "Panoramic Balcony" to Icons.Filled.Deck,
                    "Area Api Unggun" to Icons.Filled.Fireplace,
                    "Wi-Fi Gratis" to Icons.Filled.Wifi,
                    "Sarapan Organik" to Icons.Filled.FreeBreakfast,
                    "Layanan Kamar" to Icons.Filled.RoomService
                )
            )
        )
    }

    // Interactive UI State Storage
    val favorites = remember { mutableStateListOf<Int>(1, 2) }
    val bookings = remember {
        mutableStateListOf<Booking>(
            Booking(
                id = "BK-49281",
                hotel = hotelList[2],
                roomType = "Deluxe Pool View",
                checkInDate = "22 Juli 2026",
                durationNights = 2,
                totalPrice = 2500000.0,
                status = "Terkonfirmasi"
            )
        )
    }

    // Dynamic Greeting Message based on Hour
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Selamat Pagi ☀️"
            in 12..14 -> "Selamat Siang 🌤️"
            in 15..18 -> "Selamat Sore 🌅"
            else -> "Selamat Malam 🌙"
        }
    }

    // State for Booking Modal / Dialogue
    var showBookingModal by remember { mutableStateOf(false) }
    var bookingHotelTarget by remember { mutableStateOf<Hotel?>(null) }
    var selectedRoomType by remember { mutableStateOf("Standard Room") }
    var bookingNights by remember { mutableStateOf(1) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Crossfade navigation between Screens
        AnimatedContent(
            targetState = if (selectedHotel != null) "detail" else currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "screen_navigation"
        ) { targetScreen ->
            when (targetScreen) {
                "detail" -> {
                    selectedHotel?.let { hotel ->
                        HotelDetailScreen(
                            hotel = hotel,
                            isFavorite = favorites.contains(hotel.id),
                            onBack = { selectedHotel = null },
                            onToggleFavorite = {
                                if (favorites.contains(hotel.id)) {
                                    favorites.remove(hotel.id)
                                    Toast.makeText(context, "Dihapus dari Favorit", Toast.LENGTH_SHORT).show()
                                } else {
                                    favorites.add(hotel.id)
                                    Toast.makeText(context, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onBookNow = {
                                bookingHotelTarget = hotel
                                selectedRoomType = "Standard Room"
                                bookingNights = 1
                                showBookingModal = true
                            }
                        )
                    }
                }
                "cari" -> {
                    ExploreScreen(
                        hotelList = hotelList,
                        favorites = favorites,
                        greeting = greeting,
                        searchGroupQuery = searchGroupQuery,
                        onSearchChange = { searchGroupQuery = it },
                        activeCategory = activeCategory,
                        onCategoryChange = { activeCategory = it },
                        onSelectHotel = { selectedHotel = it },
                        onToggleFavorite = { hotelId ->
                            if (favorites.contains(hotelId)) {
                                favorites.remove(hotelId)
                                Toast.makeText(context, "Dihapus dari Favorit", Toast.LENGTH_SHORT).show()
                            } else {
                                favorites.add(hotelId)
                                Toast.makeText(context, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                "favorit" -> {
                    FavoritesScreen(
                        hotelList = hotelList,
                        favorites = favorites,
                        onSelectHotel = { selectedHotel = it },
                        onToggleFavorite = { hotelId ->
                            favorites.remove(hotelId)
                            Toast.makeText(context, "Dihapus dari Favorit", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "pesanan" -> {
                    BookingsScreen(
                        bookings = bookings,
                        onSelectHotel = { selectedHotel = it }
                    )
                }
                "profil" -> {
                    ProfileScreen(
                        bookingsCount = bookings.size,
                        favoritesCount = favorites.size,
                        onNavigateToHistory = { currentTab = "pesanan" },
                        onNavigateToFavorites = { currentTab = "favorit" }
                    )
                }
            }
        }

        // Beautiful Bottom Navigation Bar (Hidden when looking at Hotel Details)
        if (selectedHotel == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Subtle divider above the bottom navigation
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        icon = { Icon(if (currentTab == "cari") Icons.Filled.Search else Icons.Outlined.Search, contentDescription = "Cari") },
                        label = { Text("Cari", fontWeight = if (currentTab == "cari") FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == "cari",
                        onClick = { currentTab = "cari" },
                        modifier = Modifier.testTag("nav_tab_cari"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalNavy,
                            selectedTextColor = RoyalNavy,
                            indicatorColor = LuxuryGold.copy(alpha = 0.4f),
                            unselectedIconColor = RoyalNavy.copy(alpha = 0.6f),
                            unselectedTextColor = RoyalNavy.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentTab == "favorit") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorit") },
                        label = { Text("Favorit", fontWeight = if (currentTab == "favorit") FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == "favorit",
                        onClick = { currentTab = "favorit" },
                        modifier = Modifier.testTag("nav_tab_favorit"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalNavy,
                            selectedTextColor = RoyalNavy,
                            indicatorColor = LuxuryGold.copy(alpha = 0.4f),
                            unselectedIconColor = RoyalNavy.copy(alpha = 0.6f),
                            unselectedTextColor = RoyalNavy.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentTab == "pesanan") Icons.Filled.Assignment else Icons.Outlined.Assignment, contentDescription = "Pesanan") },
                        label = { Text("Pesanan", fontWeight = if (currentTab == "pesanan") FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == "pesanan",
                        onClick = { currentTab = "pesanan" },
                        modifier = Modifier.testTag("nav_tab_pesanan"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalNavy,
                            selectedTextColor = RoyalNavy,
                            indicatorColor = LuxuryGold.copy(alpha = 0.4f),
                            unselectedIconColor = RoyalNavy.copy(alpha = 0.6f),
                            unselectedTextColor = RoyalNavy.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentTab == "profil") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontWeight = if (currentTab == "profil") FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == "profil",
                        onClick = { currentTab = "profil" },
                        modifier = Modifier.testTag("nav_tab_profil"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalNavy,
                            selectedTextColor = RoyalNavy,
                            indicatorColor = LuxuryGold.copy(alpha = 0.4f),
                            unselectedIconColor = RoyalNavy.copy(alpha = 0.6f),
                            unselectedTextColor = RoyalNavy.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        // Custom Booking Interactive Dialogue
        if (showBookingModal && bookingHotelTarget != null) {
            val hotel = bookingHotelTarget!!
            val roomBasePriceFactor = when (selectedRoomType) {
                "Standard Room" -> 1.0
                "Deluxe Pool View" -> 1.3
                "Royal Signature Suite" -> 1.8
                else -> 1.0
            }
            val basePricePerNight = hotel.price * roomBasePriceFactor
            val subtotal = basePricePerNight * bookingNights
            val taxServiceFee = subtotal * 0.11 // 11% Tax & Service
            val totalPriceCalculated = subtotal + taxServiceFee

            Dialog(
                onDismissRequest = { showBookingModal = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Hotel, contentDescription = null, tint = RoyalNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Konfirmasi Booking",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalNavy
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { showBookingModal = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Tutup")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Mini Hotel Summary
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = hotel.imageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = LuxuryGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(hotel.location, fontSize = 12.sp, color = RoyalNavy.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Room Type selection
                        Text("Tipe Kamar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RoyalNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf("Standard Room", "Deluxe Pool View", "Royal Signature Suite").forEach { room ->
                            val isSelected = selectedRoomType == room
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        1.dp,
                                        if (isSelected) RoyalNavy else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) RoyalNavy.copy(alpha = 0.05f) else Color.Transparent)
                                    .clickable { selectedRoomType = room }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedRoomType = room },
                                    colors = RadioButtonDefaults.colors(selectedColor = RoyalNavy)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(room, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    val factor = when (room) {
                                        "Standard Room" -> 1.0
                                        "Deluxe Pool View" -> 1.3
                                        "Royal Signature Suite" -> 1.8
                                        else -> 1.0
                                    }
                                    Text(formatRupiah(hotel.price * factor) + " / malam", fontSize = 12.sp, color = RoyalNavy.copy(alpha = 0.7f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration slider or stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Durasi Menginap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RoyalNavy)
                                Text("$bookingNights Malam", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuxuryGold)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (bookingNights > 1) bookingNights-- },
                                    enabled = bookingNights > 1
                                ) {
                                    Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Kurang")
                                }
                                Text("$bookingNights", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { if (bookingNights < 14) bookingNights++ },
                                    enabled = bookingNights < 14
                                ) {
                                    Icon(Icons.Filled.AddCircleOutline, contentDescription = "Tambah")
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Billing details
                        Text("Rincian Biaya", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Harga Kamar ($bookingNights malam)", fontSize = 13.sp, color = RoyalNavy.copy(alpha = 0.7f))
                            Text(formatRupiah(subtotal), fontSize = 13.sp, color = RoyalNavy)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pajak & Biaya Pelayanan (11%)", fontSize = 13.sp, color = RoyalNavy.copy(alpha = 0.7f))
                            Text(formatRupiah(taxServiceFee), fontSize = 13.sp, color = RoyalNavy)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalNavy)
                            Text(formatRupiah(totalPriceCalculated), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LuxuryGold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Payment Button
                        Button(
                            onClick = {
                                // Add transaction to bookings list
                                val bookingId = "BK-${(10000..99999).random()}"
                                val newBooking = Booking(
                                    id = bookingId,
                                    hotel = hotel,
                                    roomType = selectedRoomType,
                                    checkInDate = "24 Juli 2026",
                                    durationNights = bookingNights,
                                    totalPrice = totalPriceCalculated,
                                    status = "Terkonfirmasi"
                                )
                                bookings.add(0, newBooking)
                                showSuccessAnimation = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_booking_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy, contentColor = Color.White)
                        ) {
                            Text("Konfirmasi & Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Custom Success Interactive Overlay Animation
        if (showSuccessAnimation) {
            Dialog(
                onDismissRequest = {
                    showSuccessAnimation = false
                    showBookingModal = false
                    selectedHotel = null
                    currentTab = "pesanan"
                },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Pemesanan Berhasil!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavy,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Kamar Anda telah sukses dipesan. Rincian e-voucher transaksi dapat dilihat di tab 'Pesanan' Anda.",
                            fontSize = 13.sp,
                            color = RoyalNavy.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                showSuccessAnimation = false
                                showBookingModal = false
                                selectedHotel = null
                                currentTab = "pesanan"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
                        ) {
                            Text("Lihat Pesanan Saya", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: EXPLORE / CARI HOTEL
// ==========================================
@Composable
fun ExploreScreen(
    hotelList: List<Hotel>,
    favorites: List<Int>,
    greeting: String,
    searchGroupQuery: String,
    onSearchChange: (String) -> Unit,
    activeCategory: String,
    onCategoryChange: (String) -> Unit,
    onSelectHotel: (Hotel) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val categories = listOf("Semua", "Populer", "Pantai", "Gunung", "Kota", "Resort")

    // Filter hotels based on category selection AND search query
    val filteredHotels = hotelList.filter { hotel ->
        val matchesCategory = activeCategory == "Semua" || hotel.category == activeCategory
        val matchesSearch = hotel.name.contains(searchGroupQuery, ignoreCase = true) ||
                hotel.location.contains(searchGroupQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp) // Offset for custom bottom navigation
    ) {
        // App Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(RoyalNavy, DeepSapphire)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                color = LuxuryGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Temukan Hotel Impianmu",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Mock Avatar with Gold Border
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .border(1.5.dp, LuxuryGold, CircleShape)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hotel_logo),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stylized M3 Search Text Field
                    OutlinedTextField(
                        value = searchGroupQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input"),
                        placeholder = { Text("Cari nama hotel, kota atau daerah...", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = LuxuryGold) },
                        trailingIcon = {
                            if (searchGroupQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Hapus", tint = Color.White)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = RoyalNavy.copy(alpha = 0.3f),
                            unfocusedContainerColor = RoyalNavy.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Horizontal Category Filter List
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Kategori Destinasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalNavy,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = activeCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(category) },
                            modifier = Modifier.testTag("category_chip_$category"),
                            label = { Text(category, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalNavy,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = SandSoft,
                                labelColor = RoyalNavy.copy(alpha = 0.8f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Hotels Vertical List
        item {
            Text(
                text = if (searchGroupQuery.isNotEmpty()) "Hasil Pencarian (${filteredHotels.size})" else "Rekomendasi Terpopuler",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RoyalNavy,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            )
        }

        if (filteredHotels.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Hotel,
                        contentDescription = null,
                        tint = RoyalNavy.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak Ada Hotel Ditemukan",
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavy,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Cobalah mencari nama daerah atau kata kunci lain.",
                        color = RoyalNavy.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            items(filteredHotels) { hotel ->
                HotelVerticalCard(
                    hotel = hotel,
                    isFavorite = favorites.contains(hotel.id),
                    onClick = { onSelectHotel(hotel) },
                    onToggleFavorite = { onToggleFavorite(hotel.id) }
                )
            }
        }
    }
}

// Individual Hotel Card Row Composable
@Composable
fun HotelVerticalCard(
    hotel: Hotel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("hotel_card_${hotel.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Hotel Image Area with overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = hotel.imageRes),
                    contentDescription = hotel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Linear Gradient Overlay for text contrast on image bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 120f
                            )
                        )
                )

                // Rating overlay (top-left)
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = LuxuryGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = hotel.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalNavy)
                }

                // Interactive bookmark heart toggle overlay (top-right)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .testTag("favorite_button_${hotel.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Simpan",
                        tint = if (isFavorite) CoralRed else RoyalNavy.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Category overlay pill (bottom-left)
                Text(
                    text = hotel.category,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(RoyalNavy.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.BottomStart)
                )
            }

            // Description Info Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = hotel.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hotel.location,
                        fontSize = 12.sp,
                        color = RoyalNavy.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Mulai dari", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatRupiah(hotel.price),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = LuxuryGold
                            )
                            Text(text = "/malam", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f), modifier = Modifier.padding(start = 2.dp, bottom = 1.dp))
                        }
                    }
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Lihat Detail", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: DETAIL HOTEL SCREEN
// ==========================================
@Composable
fun HotelDetailScreen(
    hotel: Hotel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBookNow: () -> Unit
) {
    var showMoreDesc by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp) // Leave spacing for the sticky book bottom bar
        ) {
            // Full bleed hero image header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(id = hotel.imageRes),
                    contentDescription = hotel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Visual gradient shadow top and bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.3f))
                            )
                        )
                )

                // Header buttons inside the safe zones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            .size(40.dp)
                            .testTag("back_button")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = RoyalNavy)
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (isFavorite) CoralRed else RoyalNavy
                        )
                    }
                }
            }

            // Information Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = hotel.category,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(RoyalNavy.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(hotel.rating.toString(), fontWeight = FontWeight.Bold, color = RoyalNavy, fontSize = 14.sp)
                        Text(" (${hotel.reviewsCount} Ulasan)", color = RoyalNavy.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = hotel.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavy,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(hotel.location, fontSize = 13.sp, color = RoyalNavy.copy(alpha = 0.7f))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Description
                Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hotel.description,
                    fontSize = 13.sp,
                    color = RoyalNavy.copy(alpha = 0.8f),
                    lineHeight = 19.sp,
                    maxLines = if (showMoreDesc) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (showMoreDesc) "Lihat Lebih Sedikit" else "Lihat Selengkapnya",
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { showMoreDesc = !showMoreDesc }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Amenities list
                Text("Fasilitas Unggulan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalNavy)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    hotel.amenities.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { amenity ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SandSoft, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(amenity.second, contentDescription = null, tint = RoyalNavy, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(amenity.first, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RoyalNavy)
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Booking Bar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Biaya", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatRupiah(hotel.price),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LuxuryGold
                        )
                        Text("/malam", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.5f), modifier = Modifier.padding(start = 2.dp, bottom = 2.dp))
                    }
                }

                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("book_now_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
                ) {
                    Text("Pesan Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: FAVORITES SCREEN
// ==========================================
@Composable
fun FavoritesScreen(
    hotelList: List<Hotel>,
    favorites: List<Int>,
    onSelectHotel: (Hotel) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val favoritedHotels = hotelList.filter { favorites.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoyalNavy)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = "Hotel Favorit Anda",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (favoritedHotels.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = CoralRed.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daftar Favorit Kosong",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ketuk ikon hati pada hotel yang Anda sukai untuk menyimpannya di halaman ini.",
                    fontSize = 13.sp,
                    color = RoyalNavy.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(favoritedHotels) { hotel ->
                    HotelVerticalCard(
                        hotel = hotel,
                        isFavorite = true,
                        onClick = { onSelectHotel(hotel) },
                        onToggleFavorite = { onToggleFavorite(hotel.id) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: MY RESERVATIONS / TRANSACTIONS SCREEN
// ==========================================
@Composable
fun BookingsScreen(
    bookings: List<Booking>,
    onSelectHotel: (Hotel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoyalNavy)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = "Pesanan & Tiket Saya",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (bookings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = RoyalNavy.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum Ada Riwayat Pemesanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ayo pesan hotel liburan impian Anda dan kumpulkan poin reward premium!",
                    fontSize = 13.sp,
                    color = RoyalNavy.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings) { booking ->
                    BookingCard(
                        booking = booking,
                        onClick = { onSelectHotel(booking.hotel) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${booking.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = RoyalNavy.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = booking.hotel.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(booking.hotel.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(booking.roomType, fontSize = 12.sp, color = RoyalNavy.copy(alpha = 0.7f))
                    Text("${booking.checkInDate} • ${booking.durationNights} Malam", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Dibayar", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.5f))
                    Text(formatRupiah(booking.totalPrice), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LuxuryGold)
                }
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, RoyalNavy.copy(alpha = 0.3f))
                ) {
                    Text("Detail Tiket", fontSize = 12.sp, color = RoyalNavy)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 5: USER PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    bookingsCount: Int,
    favoritesCount: Int,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoyalNavy)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(2.dp, LuxuryGold, CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hotel_logo),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Aulia Sofia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text("sofiaa.aulia@gmail.com", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GOLD MEMBER 🏅",
                    color = RoyalNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(LuxuryGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Stats Row Counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(SandSoft, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToHistory() }
            ) {
                Text(bookingsCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalNavy)
                Text("Pesanan Saya", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f))
            }
            Box(modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(RoyalNavy.copy(alpha = 0.1f))
                .align(Alignment.CenterVertically))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToFavorites() }
            ) {
                Text(favoritesCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalNavy)
                Text("Favorit", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f))
            }
            Box(modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(RoyalNavy.copy(alpha = 0.1f))
                .align(Alignment.CenterVertically))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("1.500", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalNavy)
                Text("Loyalty Poin", fontSize = 11.sp, color = RoyalNavy.copy(alpha = 0.6f))
            }
        }

        // Settings items
        Text(
            text = "Akun & Keamanan",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = RoyalNavy.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        ProfileMenuItem(icon = Icons.Filled.PersonOutline, label = "Ubah Informasi Profil")
        ProfileMenuItem(icon = Icons.Filled.CreditCard, label = "Metode Pembayaran Tersimpan")
        ProfileMenuItem(icon = Icons.Filled.Security, label = "Kata Sandi & Keamanan")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Dukungan & Informasi",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = RoyalNavy.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        ProfileMenuItem(icon = Icons.Filled.Translate, label = "Bahasa (Language)", trailingText = "Bahasa Indonesia")
        ProfileMenuItem(icon = Icons.Filled.HelpOutline, label = "Hubungi Layanan Pengguna")
        ProfileMenuItem(icon = Icons.Filled.Info, label = "Syarat & Ketentuan Layanan")

        Spacer(modifier = Modifier.height(24.dp))

        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(1.dp, CoralRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = CoralRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun", color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileMenuItem(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    trailingText: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = RoyalNavy, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 14.sp, color = RoyalNavy, modifier = Modifier.weight(1f))
        if (trailingText.isNotEmpty()) {
            Text(trailingText, fontSize = 12.sp, color = RoyalNavy.copy(alpha = 0.5f), modifier = Modifier.padding(end = 8.dp))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = RoyalNavy.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
    }
}
