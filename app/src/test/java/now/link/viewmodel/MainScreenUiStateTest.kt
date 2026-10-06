package now.link.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenUiStateTest {

    @Test
    fun defaultLoggingDisabledDialogStateIsFalse() {
        val state = MainScreenUiState()
        assertFalse(state.showLoggingDisabledDialog)
    }

    @Test
    fun copyLoggingDisabledDialogStateUpdatesCorrectly() {
        val state = MainScreenUiState()
        val updated = state.copy(showLoggingDisabledDialog = true)
        assertTrue(updated.showLoggingDisabledDialog)
        val dismissed = updated.copy(showLoggingDisabledDialog = false)
        assertFalse(dismissed.showLoggingDisabledDialog)
    }
}
