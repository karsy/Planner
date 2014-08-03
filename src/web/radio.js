(function() {

	'use strict';

	// Main AudioHandler class

	function AudioHandler(){

		// Data about the channels
		this.channels = null;
		this.channelIndex = 0;

		// Values
		this.volume = 1;
		this.paused = true;
		this.NUMBER_OF_CHANNELS = 0;

		// Timeout to avoid unnecessary loading of audio streams
		this.timeout = null;
		this.timedOut = false;

		// Elements
		this.pauseElement = null;
		this.playElement = null;
		this.volumeControl = null;
		this.volumeIconLines = null;
		this.currentChannelElement = null;
	}

	AudioHandler.prototype.initialize = function() {
		this.playElement = document.getElementById("play");
		this.playElement.addEventListener("click", function(){ this.changeSymbol.call(this, true); }.bind(this), false);

		this.pauseElement = document.getElementById("pause");
		this.pauseElement.addEventListener("click", function(){ this.changeSymbol.call(this, false); }.bind(this), false);

		this.volumeControl = document.getElementById("volumeInput");
		this.volumeControl.addEventListener("input", this.changeVolume.bind(this), false);

		var arrows = document.querySelectorAll("#channelpicker svg polygon");
		for (var i = 0; i < arrows.length; i++) {
			arrows[i].addEventListener("click", this.changeChannel.bind(this), false);
		}

		this.volumeIconLines = document.querySelectorAll("#soundLevels path");

		this.currentChannelElement = document.getElementById("currentChannel");
	};

	AudioHandler.prototype.changeSymbol = function(shouldPlayAudio) {
		if (this.paused) {
			this.pauseElement.style.visibility = "visible";
			this.playElement.style.visibility = "hidden";
			this.paused = false;
			if (shouldPlayAudio) {
				radioController.playChannel(this.channels[this.channelIndex].url);
			}
		} else {
			this.pauseElement.style.visibility = "hidden";
			this.playElement.style.visibility = "visible";
			this.paused = true;
			radioController.pauseRadio();
		}
	};

	AudioHandler.prototype.changeChannel = function(event) {

		var target = event.target;

		// Determine the channel index and show the correct logo

		if (target.parentElement.id === "right-arrow") {
			this.channelIndex++;
		} else {
			this.channelIndex--;
		}

		if (this.channelIndex >= this.NUMBER_OF_CHANNELS) {
			this.channelIndex = 0;
		} else if (this.channelIndex < 0) {
			this.channelIndex = this.NUMBER_OF_CHANNELS - 1;
		}

		this.currentChannelElement.className = this.channels[this.channelIndex].name;

		// Clear current timeout
		if (this.timedOut) {
			clearTimeout(this.timeout);
			this.timedOut = false;
		}

		// New timeout
		this.timedOut = true;
		this.timeout = setTimeout(this.changeChannelSource.bind(this), 750);
	};

	AudioHandler.prototype.changeVolume = function() {
		this.volume = this.volumeControl.valueAsNumber;

		if (this.volume < 0) {
			this.volume = 0;
		} else if (this.volume > 1) {
			this.volume = 1;
		}

		radioController.setVolume(this.volume);

		// Update the volume icon
		if (this.volume == 0) {
			updateVolumeIcon("hidden", "hidden", "hidden");
		} else if (this.volume > 0 && this.volume <= 0.33) {
			updateVolumeIcon("visible", "hidden", "hidden");
		} else if (this.volume > 0.33 && this.volume <= 0.66) {
			updateVolumeIcon("visible", "visible", "hidden");
		} else if (this.volume > 0.66) {
			updateVolumeIcon("visible", "visible", "visible");
		}
	};

	AudioHandler.prototype.changeChannelSource = function(event) {
		// Call java to load and play the radio channel
		radioController.playChannel(this.channels[this.channelIndex].url);

		if (this.paused) {
			this.changeSymbol.call(this, false);
		}
	};

	function getChannels() {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4 || xhr.readyState === 200) {
				audioHandler.channels = JSON.parse(xhr.responseText);
				audioHandler.NUMBER_OF_CHANNELS = audioHandler.channels.length;
			}
		}
		xhr.open("GET", "channels.JSON", true);
		xhr.send();
	};

	function updateVolumeIcon(arc1visibility, arc2visibility, arc3visibility) {
		audioHandler.volumeIconLines[0].style.visibility = arc1visibility;
		audioHandler.volumeIconLines[1].style.visibility = arc2visibility;
		audioHandler.volumeIconLines[2].style.visibility = arc3visibility;
	}

	var audioHandler = new AudioHandler();
	window.onload = audioHandler.initialize.bind(audioHandler);

	getChannels();
})();