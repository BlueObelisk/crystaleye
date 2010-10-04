var isClicked = "1";
var maxStructNum = "";

var imageNum = "1";
var maxImageNum = "";

function showcmd() {
	var cmd = document.getElementById("textArea").value;
	loadInJmol(cmd);
}

function showThisStructure(twoDSrc, imgNumMax, script, id) {	
	loadInJmol(script);
	changeTwodSrc(twoDSrc);
	changeStyle(id);
	maxImageNum = imgNumMax;
	document.getElementById('maxImage').innerHTML=maxImageNum;
	document.getElementById('currentImg').innerHTML=1;
	imageNum=1;
}

function changeStyle(id) {
	var node = document.getElementById(id);
	node.focus();
	setUnclickedElement(isClicked);
	node.parentNode.parentNode.className='clicked';
	isClicked=id;
}

function changeTwodSrc(pngSrc) {
	document.getElementById('twod').src=pngSrc;
	document.getElementById('currentImg').innerHTML=imageNum;
}

function loadInJmol(script) {
	jmolScript(script,0);
}

function highlightFirstStructure() {
	//note this is only used to highlight the first row of the table when the page loads
	document.getElementById(1).parentNode.parentNode.className='clicked';
}

function showUnitCells() {
	var aAxis = document.getElementById("aAxis").value;
	var bAxis = document.getElementById("bAxis").value;
	var cAxis = document.getElementById("cAxis").value;
	var cmd = "load./"+structureId+" {"+aAxis+","+bAxis+","+cAxis+"}; set forceAutoBond true;";
	loadInJmol(cmd);
}

/*-----------------------------navigation button functions---------------------------------------*/

function previousImage() {
	if (imageNum > 1) {
		imageNum--;
		var source = document.getElementById("twod").src;
		var length = source.length-11;
		source = source.substring(0,length);
		source = source+imageNum+".small.png";
		changeTwodSrc(source);
	}
}

function nextImage() {
	if (imageNum < maxImageNum) {
		imageNum++;
		var source = document.getElementById("twod").src;
		var length = source.length-11;
		source = source.substring(0,length);
		source = source+imageNum+".small.png";
		changeTwodSrc(source);
	}
}

function nextStructure() {
	if (isClicked < maxStructNum) {
		setUnclickedElement(isClicked);
		isClicked++;
		setClickedElement(isClicked);
	}
}

function previousStructure() {
	if(isClicked > 1) {
		setUnclickedElement(isClicked);
		isClicked--;
		setClickedElement(isClicked);
	}
}

function goToStructure(id) {
	setUnclickedElement(isClicked);
	setClickedElement(id);
}

function setMaxStructNum(max) {
	maxStructNum = max;
}

function setClickedElement(id) {
	isClicked=id;
	var node = document.getElementById(isClicked)
	node.parentNode.parentNode.className='clicked';
	eval(node.href);
}

function setUnclickedElement(id) {
	if(isClicked != "") {
		document.getElementById(id).parentNode.parentNode.className='unclicked';
	}
}