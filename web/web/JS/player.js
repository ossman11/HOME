// threshold results in an autopause.
// TODO(strobe): this cuts off the last N seconds of video as well; we just
// need to add some more edge cases to deal with EOF.
var MIN_BUFFERED_SECS = 1;

// Minimum amount of time required to be buffered in order to resume playback
// from an autopause.
var MIN_RESUME_SECS = 5;

var VideoURL = "videos/test/";

// Media Player functions
// init - Setup all needed global values
function init(){
	// Sets global value v to be the main video
	var video = document.getElementById('v');
	SetVideoSize(video);
	window.v = video;
	// Browser does not support the correct features
	if (!detectFeatures(video)) {
		log('Required features not detected, aborting.');
		return false;
	}
	// Load video MetaData
	LoadMeta(VideoURL+"VideoMap.vmd");
}

function SetVideoSize(vid) {
	if(vid)
	{
		var w = window.innerWidth
			|| document.documentElement.clientWidth
			|| document.body.clientWidth;
		var h = window.innerHeight
			|| document.documentElement.clientHeight
			|| document.body.clientHeight;
		if(w/(16/9)<h){
			vid.width = w;
			vid.height = w/(16/9);
		} else {
			vid.width = h*(16/9);
			vid.height = h;
		}
	}
}

// LoadMeta - Loads the video preproccessed information from server side
function LoadMeta(url){
	var xhr = new XMLHttpRequest();
	xhr.addEventListener('load', MetaOnload.bind(null, url));
	xhr.addEventListener('error', MetaError.bind(null, url));
	xhr.open("GET", url);
	xhr.send();
}
// MetaOnload - Is called after Meta Data is loaded (.onload())
function MetaOnload(url, evt){
	// check wether responce was an error code
	if(ErrorStatus(evt.target.status)){
		MetaError(url, evt);
		return;
	}
	// Parse responce to json object for ease of acces
	// original will be JSON from the start because XML is outdated.
	mpd = ParseMeta(evt.target.responseText, url);
	if(mpd == null) {
		MetaError(url, evt);
		return;
	}
	// Retrieve Video element
	var video = document.getElementById('v');
	if(video == undefined) { return; }
	MakeSource(video, mpd);
}
// MetaError - is called if Meta request fails
function MetaError(url, evt){
	log("Failed to load: " + url + " Meta Data.");
}
// ParseMeta(Meta as plane text, url) - Parse MetaFile to mpd for the MediaSource
function ParseMeta(M,U){
	/* JSON example after preproccesing by the HOME server
	{
		"duration" : "0H3M1.63S",
		"video" : [
			{"url":"FirtsVideo.mp4","MIME":"video/mp4","width":1920, "height":1080, ... },
			{"url":"SecondVideo.mp4","MIME":"video/mp4","width":1280, "height":720, ... },
			{"url":"ThirdVideo.mp4","MIME":"video/mp4","width":720, "height":480, ... }
		],
		"audio" : [
			{"url":"CrapTrack.mp4","MIME":"audio/mp4","length":53542, ... },
			{"url":"NiceTrack.mp4","MIME":"audio/mp4","length":987324, ... }
		]
	}
	*/
	return JSON.parse(M);
}

var kSlowEWMACoeff = 0.99;
var kFastEWMACoeff = 0.98;
var globalSlowBandwidth = 500000;
var globalFastBandwidth = 500000;
var last = -1;
var cooldown = 2;

// MediaSource Functions
// MakeSource() - Creates MediaSource with all added listeners
function MakeSource(vid, mpd) {
	log(mpd);
	var msrc = new MediaSource();
	msrc.mpd = mpd;
	msrc.addEventListener('sourceopen', onSourceOpen.bind(null, vid, msrc));
	msrc.addEventListener('webkitsourceopen', onSourceOpen.bind(null, vid, msrc));
	// attach to video
	var URL = window.URL;
	vid.src = URL.createObjectURL(msrc);
	window.msrc = msrc;
	// all video change handlers
	vid.addEventListener('seeking', onSeeking.bind(vid, msrc));
	vid.addEventListener('pause', onPause);
	vid.addEventListener('play', onPlay);
	vid.addEventListener('error', onError);
	// needed until custom UI is created
	vid.autopause_pending = false;
}
// onSourceOpen - gets called when mediasource is initiated and is ready
function onSourceOpen(video, msrc, evt){
	// Local Meta
	var mpd = msrc.mpd;
	// Sets timer to handle progress
	if (!msrc.progressTimer) {
		msrc.progressTimer =
		window.setInterval(onProgress.bind(video, msrc), 500);
	}
	// check for existing sourcebuffers
	if (msrc.sourceBuffers.length) {
		log("onSourceOpen(): Target already has buffers, bailing.");
		for (var i = 0; i < msrc.sourceBuffers.length; i++)
			msrc.sourceBuffers[i].active = true;
		return;
	}
	// Sets the duration of the video
	msrc.duration = getTime(mpd.duration);
	// Add video and audio sources
	addSourceBuffer(msrc, msrc.mpd["video"]);
	addSourceBuffer(msrc, msrc.mpd["audio"]);
	// updateRepresentationForm(msrc);
}
// addSourceBuffer(msrc, aset) -
function addSourceBuffer(msrc, aset) {
    // Get sub representation from the Meta (audio files / video files)
	// retrieve mime type and codecs from Meta
	var mime = aset[0].mimeType;
	var codecs = aset[0].codecs;
	// Create new sourcebuffer
	var buf = msrc.addSourceBuffer(mime + '; codecs="' + codecs + '"');
	// Append all Meta data to buffer for later referencing
	buf.aset = aset; // Full adaptation set, retained for reference
	buf.reps = aset; // copy is it is used
	buf.currentRep = 0; // Index into reps[]
	buf.active = true;  // Whether this buffer has reached EOS yet
	buf.mime = mime;
	buf.queue = [];
	// Set listener to automatically append all from queue
	if (buf.appendBuffer) {
	  buf.addEventListener('updateend', function(e) {
	    if (buf.queue.length) {
	      buf.appendBuffer(buf.queue.shift());
	    }
	  });
	}
	// Select default source to sourcebuffer
	for (var j = 0; j < aset.length; j++) {
		if (mime.indexOf('audio') > -1) {
			if (aset[j].bandwidth < 200000 && aset[j].bandwidth > 50000) {
			  buf.currentRep = j;
			  break;
			}
		}
		if (mime.indexOf('video') > -1) {
			if (aset[j].bandwidth < 1000000 && aset[j].bandwidth > 700000) {
			  buf.currentRep = j;
			  break;
			}
		}
	}
	// Clean-Up
	buf.segIdx = null;
	buf.last_init = null;  // Most-recently-appended initialization resource

	buf.resetReason = null; // Reason for performing last call to reset().
	                        // Used for better QoE when refilling after reset().
}
// updateRepresentationForm(mediasource) - start function

// resetSourceBuffer(index, reason) - resets a source buffer to re initialize
function resetSourceBuffer(buf, reason) {
	if (buf.xhr != null) {
		buf.xhr.abort();
		buf.xhr = null;
	}
	buf.url = null;
	buf.segIdx = null;
	buf.last_init = null;
	buf.reset_reason = reason || null;
	// Shame on me for using window.
	if (window.msrc.readyState != 'open') return;
	buf.abort();
}

// onProgress(video, mediasource) - timer to check for any progress
function onProgress() {
	// check wether mediasource is still open and if not stop timer.
	if (msrc.readyState != 'open' && !!msrc.progressTimer) {
		window.clearInterval(msrc.progressTimer);
		msrc.progressTimer = null;
		return;
	}
	// stop if there is an error
	if (window.v.error) return;
	var not_enough_buffered = false;
	var active = false;
	// look for an active sourcebuffer
	for (var i = 0; i < msrc.sourceBuffers.length; i++) {
		var buf = msrc.sourceBuffers[i];
		// skip
		if (!buf.active) continue;
		active = true;
		fetchNextSegment(buf, this, msrc);

		// Find the end of the current buffered range, if any, and compare that
		// against the current time to determine if we're stalling
		var range = findRangeForPlaybackTime(buf, this.currentTime);
		if (!range) {
		  not_enough_buffered = true;
		} else if (this.paused) {
		  not_enough_buffered |= (range.end < this.currentTime + MIN_RESUME_SECS);
		} else {
		  not_enough_buffered |= (range.end < this.currentTime + MIN_BUFFERED_SECS);
		}
	}
	// No active Sources found but mediasource is still open so is being closed
	if (!active && msrc.readyState == 'open') {
		msrc.endOfStream();
		return;
	}

	if (this.paused) {
		if (this.autopaused) {
			// Last pause was an autopause, decide if we should resume
			if (!not_enough_buffered) {
				this.play();
			}
		}
	} else {
		if (not_enough_buffered) {
			this.autopause_pending = true;
			this.pause();
		}
	}
}
// fetchNextSegment(sourcebuffer, video element, mediaSource) - Requests new piece of the video
function fetchNextSegment(buf, video, msrc) {
	if (buf.xhr) return;
	var time = video.currentTime;
	var rep = buf.reps[buf.currentRep];

	if (rep.init.value == null) {
		var xhr = makeXHR(buf, VideoURL + rep.url, rep.init.start, rep.init.end, rep.init, true, false);
		return;
	}

	if (rep.index == null) {
		var xhr = makeXHR(buf, VideoURL + rep.url, rep.indexRange.start, rep.indexRange.end, rep.init, false, true);
		return;
	}
	if (adapt(msrc.mpd)) return;

	var range = findRangeForPlaybackTime(buf, time);
	var append_time = (range && range.end) || time;
	if (append_time > time + 15) return;

	if (buf.segIdx == null) {
		buf.segIdx = Math.max(0, rep.index.findForTime(append_time));
	} else {
		if (range == null) {
			log("Current playback head outside of buffer in append-continue state.");
		}
	}
	var offset = rep.index.getOffset(buf.segIdx);
	var size = rep.index.getByteLength(buf.segIdx);
	var xhr = makeXHR(buf, VideoURL + rep.url, offset, offset + size - 1, rep.init, false, false);
	xhr.expected_time = append_time;
}
// makeXHR(sourcebuffer, target url, startbytes, endbytes, init x3) - Creates request
function makeXHR(buf, url, start, end, init_ref, is_init, is_index) {
	var xhr = new XMLHttpRequest();
	var range = mkrange(start, end);
	var useArg = !!/youtube.com/.exec(url)
	if (range && useArg) {
		url = url.replace(/&range=[^&]*/, '');
		url += '&range=' + range.substring(6);
	}
	xhr.open("GET", url);
	xhr.responseType = 'arraybuffer';
	xhr.startByte = start;
	if (range != null && !useArg) xhr.setRequestHeader('Range', range);
		xhr.addEventListener('load', onXHRLoad);
	if (url == null) throw "Null URL";
	buf.url = url;
	xhr.buf = buf;
	xhr.init = init_ref;
	xhr.is_init = is_init;
	xhr.is_index = is_index;
	buf.xhr = xhr;
	xhr.lastTime = null;
	xhr.lastSize = null;
	xhr.addEventListener('progress', onXHRProgress);
	xhr.send();
	log('Sent XHR: url=' + url + ', range=' + range);
	return xhr;
}

function onXHRLoad(evt) {
	var xhr = evt.target;
	var buf = xhr.buf;
	buf.xhr = null;
	var vid = buf.video;

	if (xhr.readyState != xhr.DONE) return;
	if (xhr.status >= 300) {
		log('XHR failure, status=' + xhr.status);
		throw 'TODO: retry XHRs on failure';
	}

	if (xhr.is_init) {
		xhr.init.value = new Uint8Array(xhr.response);
	}

	if (xhr.is_index) {
		var index = new player.dash.SegmentIndex();
		buf.reps[buf.currentRep].index = index;
		if (buf.mime.indexOf('mp4') >= 0) {
			index.parseSidx(xhr.response, xhr.startByte);
		} else {
			index.parseWebM(xhr.init.value.buffer, xhr.response);
		}
		// We need the index before we append the init.
		if (buf.last_init == null) appendInit(buf, xhr.init);
	}

	if (xhr.is_init || xhr.is_index) {
		return;
	}

	if (buf.last_init !== xhr.init) {
		appendInit(buf, xhr.init);
	}

	queueAppend(buf, xhr.response);
	buf.segIdx++;

	if (buf.segIdx >= buf.reps[buf.currentRep].index.getCount()) {
		buf.active = false;
	}

	if (xhr.expected_time != null && !buf.appendBuffer) {
		// The expected time is the start time of the first buffer in this sequence.
		// This check ensures that media data append time is (roughly) reflected in
		// the buffered range.
		range = findRangeForPlaybackTime(buf, xhr.expected_time);
		if (range == null || !(range.start <= xhr.expected_time && range.end >= xhr.expected_time)) {
			log('Media data expected time not reflected in updated buffer range. ' +
			'MSE implementation bug?');
			if (range == null) {
				log( 'Reason: range is null');
			} else {
				log( 'Reason: expected time ' + xhr.expected_time + ' not in (' +
				range.start + ', ' + range.end + ')');
			}
		}
	}
}

function onXHRProgress(evt) {
	var xhr = evt.target;
	if (xhr.lastTime != null && evt.timeStamp != xhr.lastTime) {
		var bw = 8000 * (evt.loaded - xhr.lastSize) / (evt.timeStamp - xhr.lastTime);
		globalSlowBandwidth = kSlowEWMACoeff * globalSlowBandwidth + (1 - kSlowEWMACoeff) * bw;
		globalFastBandwidth = kFastEWMACoeff * globalFastBandwidth + (1 - kFastEWMACoeff) * bw;
	}
	xhr.lastTime = evt.timeStamp;
	xhr.lastSize = evt.loaded;
}

function adapt(mpd) {
	// return false;
	if (cooldown) {
		cooldown--;
	} else {
		var bestBw = 0;
		var best = 0;
		var gbw = Math.min(globalSlowBandwidth, globalFastBandwidth);
		for (var i = 0; i < mpd.video.length; i++) {
			var bw = mpd.video[i].bandwidth;

			if (bw > bestBw && bw < (0.85 * gbw - 128000)) {
				bestBw = bw;
				best = i;
			}
		}
		if(last != best){
			last=best;
			ChangeRep(msrc.sourceBuffers[0], best);
			return true;}
	}
	return false;
}

function ChangeRep(buf, target) {
	var reason;
	var oldBW = buf.aset[buf.currentRep].bandwidth;
	var newBW = buf.aset[target].bandwidth;
	// TEMPORARY HACK: ignore change if rep is 32k (HE-AACv2).
	if (newBW < 48000) return;
	if (oldBW > newBW) {
		reason = 'rep_down';
	} else {
		reason = 'rep_up';
	}
	resetSourceBuffer(buf, reason);
	buf.currentRep = target;
}
// Global MediaSource Functions
// findRangeForPlaybackTime - ...
function findRangeForPlaybackTime(buf, time) {
	var ranges = buf.buffered;
	for (var i = 0; i < ranges.length; i++) {
		if (ranges.start(i) <= time && ranges.end(i) >= time) {
		  return {'start': ranges.start(i), 'end': ranges.end(i)};
		}
	}
}
// mkrange(start, end) - formats a start and end point into a byte range format
function mkrange(start, end) {
  if (start != null && end != null) return 'bytes=' + start + '-' + end;
  return null;
}
// appendInit - add the init for the right stream to the buffer
function appendInit(buf, init) {
	log("Appending init");
	queueAppend(buf, init.value);
	buf.last_init = init;
	buf.timestampOffset = -buf.reps[buf.currentRep].index.getStartTime(0);
}
// queueAppend - adds the buffer to the queue to be added to the sourcebuffer
function queueAppend(buf, val) {
	if (buf.updating) {
		buf.queue.push(val);
	} else if (buf.appendBuffer) {
		buf.appendBuffer(val);
	} else {
		buf.append(new Uint8Array(val));
	}
}

// video handlers
// onSeeking(video element, MediaSource) - Called when changing video time
function onSeeking(vid, msrc){
	// TODO(strobe): Build quality index, other abstractions so that we know which
	// range is currently being appended explicitly and whether we should reset
	for (var i = 0; i < msrc.sourceBuffers.length; i++)
    	resetSourceBuffer(msrc.sourceBuffers[i], 'seeking');
}
// onPause - called when the video is paused
function onPause(evt){
	this.autopaused = this.autopause_pending;
	this.autopause_pending = false;
}
// onPlay - called when the video is resumed
function onPlay(evt){
	this.autopause_pending = false;
	this.autopaused = false;
}
// onError - called when the video element runs into an error
function onError(evt){
	log('Error (' + v.error.code + ')');
}

// Global Functions
// log(message) - send message to console
function log(msg) {
	console.log(msg);
}
// ErrorStatus(responce status) - Check status code to be an error (if error return true else false)
function ErrorStatus(e){
	if( e<200 || e>299 ){return true;}
	return false;
}
// detectFeatures(video element) - check wether browser supports the video player
function detectFeatures(video) {
	var ok = true;
	if (!hasMediaSource()) {
		log('MSE not detected.');
		ok = false;
	}
	return ok;
}
// hasMediaSource - checks wether MediaSource is defined
function hasMediaSource(){
	if(MediaSource == undefined) {
		return false;
	}
	return true;
}
// getTime(timeStamp) - converts the time to seconds
function getTime(date){
	var pat = /(([0-9]*)H)?(([0-9]*)M)?(([0-9.]*)S)?/;
	var match = pat.exec(date);
	if (!match) return parseFloat(date);
	return (parseFloat(match[2] || 0) * 3600 +
        	parseFloat(match[4] || 0) * 60 +
        	parseFloat(match[6] || 0));
}