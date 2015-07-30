window.onload = function(){
	// Create eventlisteners for head buttons
	var TMenu = document.getElementById("TMenu");
	TMenu.addEventListener('click', ToggleMenu.bind(null, false));
	var TNoti = document.getElementById("TNoti");
	TNoti.addEventListener('click', ToggleNoti.bind(null, false));
	
	//catch hyperlinks
	var HyperLinks = document.querySelectorAll( 'a' );
	for( var i=HyperLinks.length; i--; ) {
		HyperLinks[i].addEventListener( 'click', LoadLink.bind(null, HyperLinks[i].attributes.getNamedItem("href").value) );
	}
	
	var MMenu = document.getElementById("MMenu");
}

// Menu functions
function ToggleMenu(NState, evt){
	var TClass = document.getElementById("TMenu").attributes.getNamedItem('class');
	var MClass = document.getElementById("MMenu").attributes.getNamedItem('class');
	
	if(!NState){
		NState = (MClass.value.indexOf('Open') > -1);
	}
	if(NState){
		MClass.value = "MastMenu";
		TClass.value = "ToggleMenu";
	} else {
		ToggleNoti(true);
		MClass.value = "MastMenu Open";
		TClass.value = "ToggleMenu Open";
	}
}

function ToggleNoti(NState, evt){	
	var TClass = document.getElementById("TNoti").attributes.getNamedItem('class');
	var MClass = document.getElementById("MNoti").attributes.getNamedItem('class');
	
	if(!NState){
		NState = (MClass.value.indexOf('Open') > -1);
	}
	if(NState){
		MClass.value = "MastNoti";
		TClass.value = "ToggleNoti";
	} else {
		ToggleMenu(true);
		MClass.value = "MastNoti Open";
		TClass.value = "ToggleNoti Open";
	}
}

function LoadLink(url, e){
	if(e.button == 1){return;}
	e.preventDefault();
	console.log(url);
	// Start loading bar
	LoadingBar(true);
	var req = new XMLHttpRequest();
	req.addEventListener('load', OnLinkLoad.bind(null, url));
	req.addEventListener('error', OnLinkError.bind(null, url));
	req.open("GET", "/PLAIN" + url);
	req.send();
}

function OnLinkError(url, evt){
	console.log("Failed to Load.");
	//LoadingBar(false);
}

function OnLinkLoad( url, evt){
	console.log(evt);
	// Update UI
	SetUrl(url);
	SelectMenuItem(url);
	ToggleMenu(true);
	// Update Content
	document.getElementById('MCont').children[0].innerHTML = evt.target.responseText;
	// Remove Loading bar
	LoadingBar(false);
}

function SelectMenuItem(url){
	// Select Menu Items
	var items = document.getElementById('MMenu').children;
	for(var i = 0; i < items.length; i++){
		// get current item
		var cur = items[i];
		var curClass = cur.attributes.getNamedItem('class');
		var curHref = cur.attributes.getNamedItem('href');
		// Check if current url
		if(curHref.value == url){
			curClass.value = "MenuItem Cur"
		} else {
			curClass.value = "MenuItem";
		}
	}
}

function SetUrl(url){
	window.history.pushState({},"", url);
	var title = url.substring(url.lastIndexOf("/")+1);
	if(title != ""){
		title = "HOME - " + title;
	}else{
		title = "HOME";
	}
	document.title = title;
}

function LoadingBar(State){
	var LB = document.getElementById('LBar').attributes.getNamedItem('class');
	if(State){
		LB.value = "LoadingBar Loading";
	} else {
		LB.value = "LoadingBar";
	}
}

// Scaling functions