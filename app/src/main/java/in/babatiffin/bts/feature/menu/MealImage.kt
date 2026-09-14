package com.babatiffin.bts.feature.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.babatiffin.bts.R
import com.babatiffin.bts.data.menu.Meal
import kotlin.math.roundToInt

private const val ATLAS_COLUMNS = 6
private const val SOURCE_WIDTH = 96
private const val SOURCE_HEIGHT = 72

@Composable
fun MealImage(meal: Meal, modifier: Modifier = Modifier) {
    val imageIndex = remember(meal.name) { mealImageIndex(meal.name) }
    if (imageIndex != null) {
        val atlas = ImageBitmap.imageResource(R.drawable.meal_atlas)
        Canvas(modifier.semantics { contentDescription = meal.name }) {
            drawImage(
                image = atlas,
                srcOffset = IntOffset((imageIndex % ATLAS_COLUMNS) * SOURCE_WIDTH, (imageIndex / ATLAS_COLUMNS) * SOURCE_HEIGHT),
                srcSize = IntSize(SOURCE_WIDTH, SOURCE_HEIGHT),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            )
        }
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Text(meal.name.take(1), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun mealImageIndex(name: String): Int? {
    val normalized = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
    return imageIndexByKey[aliases[normalized] ?: normalized]
}

private val imageIndexByKey = (
    "aloo_matar besan_chilla boiled_eggs bread_slices butter_chicken butter chaas chana_dal " +
        "chicken_biryani chicken_curry chicken_kadhai chicken_keema chicken_masala chicken_pulao chicken_stew " +
        "chole_poori chole coconut_chutney curd dal_fry dal_makhani dal_tadka dalia dry_fruits egg_bhurji " +
        "egg_curry egg_fried_rice egg_masala egg_sandwich fish_curry fish_fry idli_sambar jeera_aloo jeera_rice " +
        "kadhi_pakora khichdi lauki_chana_dal masala_khichdi masala_omelette matar_paneer methi_paratha milk " +
        "mix_veg mustard_fish mutton_biryani mutton_curry onion_salad paneer_chilla paneer_curry paneer_masala " +
        "paneer_paratha paneer_poha paneer_sandwich papad paratha_curd pickle poha poori_sabzi raita rajma rice " +
        "roti salad sarson_saag seasonal_vegetables shahi_paneer soya_curry tea veg_biryani veg_sandwich veg_upma"
    ).split(' ').withIndex().associate { it.value to it.index }

private val aliases = mapOf(
    "1_day_2_meals_free" to "chicken_biryani",
    "3_day_meal_streak" to "idli_sambar",
    "subscribe_save" to "shahi_paneer",
    "refer_eat" to "veg_biryani",
    "bts_loyalty_streak" to "paneer_curry",
    "2_boiled_eggs" to "boiled_eggs",
    "aloo_paratha_with_curd" to "paratha_curd",
    "boondi_veg_raita" to "raita",
    "extra_boiled_egg" to "boiled_eggs",
    "extra_rice" to "rice",
    "extra_roti" to "roti",
    "fresh_salad" to "salad",
    "khichdi_with_curd_papad" to "khichdi",
    "methi_palak_paratha_with_curd" to "methi_paratha",
    "mustard_fish_curry" to "mustard_fish",
    "roasted_papad" to "papad",
    "rohu_katla_fish_fry" to "fish_fry",
    "sarson_ka_saag" to "sarson_saag",
    "soya_matar_curry" to "soya_curry",
    "steamed_rice" to "rice",
    "vegetable_dalia" to "dalia",
)
