package com.njbrady.nusic.upload

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.njbrady.nusic.R
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.upload.model.UploadSongRepository
import com.njbrady.nusic.upload.requests.uploadSong
import com.njbrady.nusic.utils.ExoPlayerPositionTracker
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.calculateAmplitudes
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadScreenViewModel @Inject constructor(
    private val _localStorage: LocalStorage,
    @DefaultDispatcher private val _defaultDispatcher: CoroutineDispatcher,
    context: Context
) : ViewModel() {

    private val _songTitle = MutableStateFlow("")
    private val _songPhotoUrl = MutableStateFlow<Uri?>(null)
    private val _songUrl = MutableStateFlow<Uri?>(null)
    private val _songAmplitude = MutableStateFlow(emptyList<Float>())
    private val _uploadSongLoading = MutableStateFlow(false)
    private val _uploadSongStartTime = MutableStateFlow(0)
    private val _uploadSongEndTime = MutableStateFlow(30)
    private val _uploadSongCurPos = MutableStateFlow<Int?>(null)
    private val _uploadSongPlayerState = MutableStateFlow(PlayerState.Playing)
    private val _songErrors = MutableStateFlow<List<String>?>(null)
    private val _photoErrors = MutableStateFlow<List<String>?>(null)
    private val _titleErrors = MutableStateFlow<List<String>?>(null)
    private val _timeErrors = MutableStateFlow<List<String>?>(null)
    private val _generalErrors = MutableStateFlow<List<String>?>(null)
    private val _successfulUpload = MutableStateFlow(false)
    private val _generalLoading = MutableStateFlow(false)
    val _currentlySelected = MutableStateFlow(SongListType.Liked)

    val currentlySelected: StateFlow<SongListType> = _currentlySelected
    val username = _localStorage.retrieveUsername()
    val songTitle: StateFlow<String> = _songTitle
    val songPhotoUrl: StateFlow<Uri?> = _songPhotoUrl
    val songUrl: StateFlow<Uri?> = _songUrl
    val songAmplitude: StateFlow<List<Float>> = _songAmplitude
    val uploadSongLoading: StateFlow<Boolean> = _uploadSongLoading
    val uploadSongStartTime: StateFlow<Int> = _uploadSongStartTime
    val uploadSongEndTime: StateFlow<Int> = _uploadSongEndTime
    val uploadSongPlayerState: StateFlow<PlayerState> = _uploadSongPlayerState
    val uploadSongCurPos: StateFlow<Int?> = _uploadSongCurPos
    val songErrors: StateFlow<List<String>?> = _songErrors
    val photoErrors: StateFlow<List<String>?> = _photoErrors
    val titleErrors: StateFlow<List<String>?> = _titleErrors
    val timeErrors: StateFlow<List<String>?> = _timeErrors
    val generalErrors: StateFlow<List<String>?> = _generalErrors
    val successfulUpload: StateFlow<Boolean> = _successfulUpload
    val generalLoading: StateFlow<Boolean> = _generalLoading

    var currentSong: ExoPlayer = ExoPlayer.Builder(context).build()
    var mediaPlayerTracker: ExoPlayerPositionTracker =
        ExoPlayerPositionTracker(currentSong, { position ->
            _uploadSongCurPos.value = position
        })

    init {
        currentSong.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> if (_uploadSongPlayerState.value == PlayerState.Loading) _uploadSongPlayerState.value =
                        PlayerState.Paused else _uploadSongPlayerState.value = PlayerState.Playing
                    Player.STATE_BUFFERING -> _uploadSongPlayerState.value = PlayerState.Loading
                    Player.STATE_ENDED -> _uploadSongPlayerState.value = PlayerState.Paused
                    else -> {}
                }
            }
        })
    }

    fun togglePlayState() {
        when (uploadSongPlayerState.value) {
            PlayerState.Playing -> {
                currentSong.pause()
                _uploadSongPlayerState.value = PlayerState.Paused
            }
            PlayerState.Paused -> {
                currentSong.seekTo((uploadSongStartTime.value * 1000).toLong())
                currentSong.play()
                _uploadSongPlayerState.value = PlayerState.Playing
            }
            else -> {}
        }
    }

    fun setCurrentlySelected(songListType: SongListType) {
        _currentlySelected.value = songListType
    }
    fun resetErrors() {
        _generalErrors.value = null
        _timeErrors.value = null
        _photoErrors.value = null
        _songErrors.value = null
        _titleErrors.value = null
    }

    fun attemptUpload(context: Context) {
        pauseWhenReady()
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _generalLoading.value = true
                val curSongUrl = songUrl.value
                val curPhotoUrl = songPhotoUrl.value
                val repo = if (curSongUrl != null && curPhotoUrl != null) {
                    uploadSong(
                        songTitle = songTitle.value,
                        songAudio = curSongUrl,
                        songPhoto = curPhotoUrl,
                        context = context,
                        start = uploadSongStartTime.value,
                        end = uploadSongEndTime.value,
                        localStorage = _localStorage
                    )
                } else {
                    UploadSongRepository(
                        containsError = true,
                        songPhotoErrors = if (curPhotoUrl == null) listOf("No photo selected") else null,
                        songErrors = if (curSongUrl == null) listOf("No song selected") else null
                    )
                }
                if (repo.containsError) {
                    _timeErrors.value = repo.timeErrors
                    _titleErrors.value = repo.titleErrors
                    _songErrors.value = repo.songErrors
                    _photoErrors.value = repo.songPhotoErrors
                    _generalErrors.value = repo.generalErrors
                } else {
                    _successfulUpload.value = true
                }
                _generalLoading.value = false
            }
        }
    }

    fun pauseWhenReady() {
        if (currentSong.isPlaying) {
            currentSong.pause()
            _uploadSongPlayerState.value = PlayerState.Paused
        }
    }

    fun clearToastErrors() {
        _generalErrors.value = null
    }

    fun setSongTitle(title: String) {
        _songTitle.value = title
    }

    fun setPhotoUrl(uri: Uri) {
        _songPhotoUrl.value = uri
    }

    fun setStartTime(int: Int) {
        _uploadSongStartTime.value = int
        _uploadSongEndTime.value = int + 30
    }

    fun setSongUrl(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _songUrl.value = uri
                _uploadSongLoading.value = true
                calculateAmplitudes(context = context, uri = uri) { amplitudes ->
                    Handler(Looper.getMainLooper()).post {
                        _uploadSongLoading.value = false
                        _uploadSongPlayerState.value = PlayerState.Loading
                        setStartTime(0)
                        _uploadSongCurPos.value = null
                        _songAmplitude.value = amplitudes
                        mediaPlayerTracker.stopTracking()
                        currentSong.removeMediaItem(0)
                        currentSong.setMediaItem(MediaItem.fromUri(uri))
                        currentSong.prepare()
                        mediaPlayerTracker.startTracking()
                    }
                }
            }
        }
    }

    fun clearPlayerState() {
        mediaPlayerTracker.stopTracking()
        currentSong.pause()
        currentSong.removeMediaItem(0)
    }

    fun clearState() {
        _songTitle.value = ""
        resetErrors()
        _songPhotoUrl.value = null
        _songUrl.value = null
        setStartTime(0)
        _uploadSongCurPos.value = null
        _successfulUpload.value = false
        _generalLoading.value = false
        clearPlayerState()
    }

    override fun onCleared() {
        super.onCleared()
        currentSong.release()
    }

    companion object {
        val DEFAULT_SONG = listOf<Float>(
            1.0f,
            0.5382664f,
            0.47341347f,
            0.42558905f,
            0.24238613f,
            0.35731608f,
            0.43221363f,
            0.4561261f,
            0.19902521f,
            0.49364164f,
            0.14699723f,
            0.35820103f,
            0.6619167f,
            0.32534066f,
            0.5050008f,
            0.3586375f,
            0.515491f,
            0.5154416f,
            0.38869616f,
            0.5429059f,
            0.44341317f,
            0.47206655f,
            0.5998971f,
            0.5174936f,
            0.28856072f,
            0.65694225f,
            0.35296416f,
            0.39047444f,
            0.6387219f,
            0.35158694f,
            0.32503492f,
            0.42013976f,
            0.31710935f,
            0.45808804f,
            0.65692526f,
            0.49926603f,
            0.35014406f,
            0.4521839f,
            0.5952218f,
            0.2786577f,
            0.5703275f,
            0.19452325f,
            0.6099259f,
            0.5325863f,
            0.60627204f,
            0.49050382f,
            0.38458267f,
            0.44871408f,
            0.42619032f,
            0.37882358f,
            0.48556885f,
            0.3755568f,
            0.3993323f,
            0.37748843f,
            0.2665652f,
            0.29731655f,
            0.35623989f,
            0.5646323f,
            0.44863364f,
            0.42468897f,
            0.27509657f,
            0.2809337f,
            0.2773072f,
            0.25427127f,
            0.6023756f,
            0.553159f,
            0.30621025f,
            0.6018763f,
            0.29925242f,
            0.41835082f,
            0.25509346f,
            0.65837f,
            0.23290853f,
            0.33481255f,
            0.34818217f,
            0.2702207f,
            0.47845784f,
            0.4967047f,
            0.5564181f,
            0.4051919f,
            0.5804264f,
            0.11343715f,
            0.50581056f,
            0.50986916f,
            0.57369554f,
            0.53190076f,
            0.6159492f,
            0.20764756f,
            0.2619739f,
            0.44068244f,
            0.39111838f,
            0.4422623f,
            0.27569678f,
            0.32532212f,
            0.40570992f,
            0.39686608f,
            0.4208289f,
            0.39745674f,
            0.41749102f,
            0.4965004f,
            0.44168323f,
            0.7109788f,
            0.23446125f,
            0.36071634f,
            0.34061387f,
            0.6259153f,
            0.5078579f,
            0.3794497f,
            0.4061772f,
            0.5288544f,
            0.5032355f,
            0.49402353f,
            0.5452653f,
            0.32147646f,
            0.54595536f,
            0.12508528f,
            0.47033295f,
            0.481527f,
            0.35610458f,
            0.3825723f,
            0.49212828f,
            0.40053383f,
            0.23517182f,
            0.3109056f,
            0.18791117f,
            0.41813698f,
            0.4362328f,
            0.39929175f,
            0.5092795f,
            0.43547308f,
            0.43251115f,
            0.43656158f,
            0.45352975f,
            0.4676644f,
            0.5266877f,
            0.31619924f,
            0.5744739f,
            0.4012339f,
            0.49713454f,
            0.45194033f,
            0.43331072f,
            0.44292954f,
            0.22629045f,
            0.57722867f,
            0.22702073f,
            0.39782596f,
            0.73415613f,
            0.6093031f,
            0.25947413f,
            0.30522484f,
            0.32240084f,
            0.61396575f,
            0.49304643f,
            0.079222746f,
            0.0f,
            0.65170383f,
            0.40208846f,
            0.45908585f,
            0.5146526f,
            0.21768439f,
            0.59446394f,
            0.33474848f,
            0.48024508f,
            0.32168382f,
            0.39547947f,
            0.5695157f,
            0.4226501f,
            0.37050197f,
            0.45316792f,
            0.22510736f,
            0.34286454f,
            0.4203136f,
            0.4632354f,
            0.39191258f,
            0.24891672f,
            0.34659493f,
            0.64435637f,
            0.56811434f,
            0.1747623f,
            0.4303222f,
            0.5412099f,
            0.39614043f,
            0.26980302f,
            0.44016626f,
            0.340815f,
            0.4860241f,
            0.329357f,
            0.54132193f,
            0.6643113f,
            0.40473992f,
            0.7134459f,
            0.4271878f,
            0.49465045f,
            0.31619182f,
            0.4188217f,
            0.42617336f,
            0.30227983f,
            0.57794833f,
            0.38872066f,
            0.5073479f,
            0.3249386f,
            0.35953957f,
            0.433964f,
            0.38432857f,
            0.45183244f,
            0.3774781f,
            0.28622282f,
            0.62708324f,
            0.473508f,
            0.48501557f,
            0.46643373f,
            0.35439748f,
            0.46940994f,
            0.34855527f,
            0.5267841f,
            0.52469814f,
            0.55815816f,
            0.4659432f,
            0.5724185f,
            0.455012f,
            0.5059268f,
            0.14854284f,
            0.50207937f,
            0.5591189f,
            0.63226897f,
            0.36470103f,
            0.5943585f,
            0.5562886f,
            0.5001633f,
            0.48908868f,
            0.33243585f,
            0.4186657f,
            0.35136193f,
            0.37245634f,
            0.5019566f,
            0.57085645f,
            0.44588217f,
            0.25228083f,
            0.17079045f,
            0.5808144f,
            0.2181519f,
            0.4167581f,
            0.18410346f,
            0.60655266f,
            0.2411334f,
            0.55920714f,
            0.41601947f,
            0.6096726f,
            0.43145284f,
            0.4503071f,
            0.5479603f,
            0.46387902f,
            0.31616664f,
            0.6110175f,
            0.66540927f,
            0.42362085f,
            0.5943f,
            0.3677395f,
            0.39021724f,
            0.24965034f,
            0.28993192f,
            0.59615606f,
            0.34439412f,
            0.5181472f,
            0.44764218f,
            0.46960172f,
            0.5986315f,
            0.599856f,
            0.34854972f,
            0.64256376f,
            0.41720298f,
            0.5716646f,
            0.42986584f,
            0.53634536f,
            0.23332383f,
            0.478012f,
            0.41944712f,
            0.4204446f,
            0.47111318f,
            0.33921263f,
            0.4038418f,
            0.47490942f,
            0.33083707f,
            0.5204404f,
            0.31743503f,
            0.50233966f,
            0.54042447f,
            0.24201918f,
            0.45818454f,
            0.52364045f,
            0.27878535f,
            0.34011862f,
            0.39626035f,
            0.42166138f,
            0.5821167f,
            0.5886835f,
            0.14853773f,
            0.43806833f,
            0.37350154f,
            0.61993766f,
            0.687147f,
            0.6423195f,
            0.65214646f,
            0.52374804f,
            0.27691916f,
            0.724034f,
            0.2892434f,
            0.5473024f,
            0.48122323f,
            0.701957f,
            0.31811965f,
            0.5819553f,
            0.42771447f,
            0.513711f,
            0.5223404f,
            0.2796473f,
            0.4997893f,
            0.37511802f,
            0.47863582f,
            0.1612977f,
            0.49803644f,
            0.62611043f,
            0.2781862f,
            0.37926263f,
            0.4742588f,
            0.4429995f,
            0.3343333f,
            0.47629443f,
            0.32667592f,
            0.57370406f,
            0.6339866f,
            0.55510896f,
            0.39901757f,
            0.3360227f,
            0.1826129f,
            0.48381022f
        )
    }
}

enum class UploadScreenState {
    UploadSong, UploadPhoto, SongTitle, Preview
}

enum class UploadScreenAuxState {
    Passive, Loading
}

enum class PlayerState {
    Playing, Paused, Loading, Error, Completed
}

fun UploadScreenState.stringResource(): Int {
    return when (this) {
        UploadScreenState.UploadPhoto -> R.string.upload_photo
        UploadScreenState.UploadSong -> R.string.upload_song
        UploadScreenState.SongTitle -> R.string.song_title
        UploadScreenState.Preview -> R.string.song_card_preview
    }
}