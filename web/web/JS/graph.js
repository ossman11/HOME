/**
 * public class Graph
 * javascript side for interactive graphs
 */
var Graph = function(type, val, dom) {
	this.Type = type;
	this.Values = val;
	this.Total = this.Total();
	this.DOM = dom;
	this.Draw();
}

/**
 * Drawing functions
 * Draw the graph in the DOM element
 */
Graph.prototype.Draw = function(){
	if(this.DOM && this.Type){
		if(this.Type == "BAR")
		{
			var h = "<div class='Data'><div></div>";
			var t = "<div class='Tags' style='margin-top:-"+ (this.DOM.offsetHeight-10) +"px;'>";
			for(var i = 0; i < this.Values.length; i++){
				h += this.CBar(i);
				t += this.CTag(i);
			}
			h += "</div>";
			t += "</div>";

			this.DOM.innerHTML = h+t;
		} else if(this.Type == "PIE"){
			var s = 0;
			if(this.DOM.offsetWidth < this.DOM.offsetHeight){
				s = this.DOM.offsetWidth;
			} else {
				s = this.DOM.offsetHeight;
			}
			var h = "<div class='Data' style='width:"+ s +"px;height:"+ s +"px;margin-bottom:"+ s +"px;'><div style='width:100%;height:100%;'></div>";
			var t = "<div class='Tags'>";
			var last = 0;
			for(var i = 0; i < this.Values.length; i++){
				h += this.CPie(last,last+this.Get(i,true)*100,this.Values[i]['col']);
				last += this.Get(i,true)*100;
				t += this.CTag(i);
			}
			h += "</div>";
			t += "</div>";

			this.DOM.innerHTML = h+t;
		} else if(this.Type == "CIR"){
			var s = 0;
			if(this.DOM.offsetWidth < this.DOM.offsetHeight){
				s = this.DOM.offsetWidth;
			} else {
				s = this.DOM.offsetHeight;
			}
			var h = "<div class='Data' style='width:"+ s +"px;height:"+ s +"px;margin-bottom:"+ s +"px;'><div style='width:100%;height:100%;'></div>";
			var t = "<div class='Tags'>";
			var last = 0;
			for(var i = 0; i < this.Values.length; i++){
				h += this.CPie(last,last+this.Get(i,true)*100,this.Values[i]['col']);
				last += this.Get(i,true)*100;
				t += this.CTag(i);
			}
			h += "<div class='GC'><div class='GCC'></div></div>";
			h += "</div>";
			t += "</div>";

			this.DOM.innerHTML = h+t;
		}
	}
}
/* 
 * CBar Creates a single Graph bar
 */
Graph.prototype.CBar = function(id){
	var w = this.DOM.offsetWidth/(this.Values.length*4);
	var h = this.DOM.offsetHeight;
	var ret = "<div id='BAR-"+ this.Values[id]["tag"] +"' class='BAR' " +
		"style='width:"+ (w*2) +"px;"+
		"margin:0px "+ w +"px;"+
		"margin-top:"+ (h-(this.Get(id,true)*h)) +"px;"+
		"height:"+ (this.Get(id,true)*h) +"px;";
	if(this.Values[id]["col"])
	{
		ret += "background-color:"+ this.Values[id]["col"] +";";
	}
	ret += "'></div>";
	return ret;
}
/** 
 * CPie Creates a single Pie layer
 */
Graph.prototype.CPie = function (S,E,col){
	if(S==0 && E<0){S=100+E;E=100;}
	var ret = '<div class="GC">';
	var C = 90/25;

	var R = 0;
	var O = 0;
	if(E<=75 || S>=100){R = 0;}else
	{
		if(S<=75)
		{
			R = 90+(C*(E-75));
		}
		else
		{
			if(E<100)
			{
				O = ((100-E)*C);
				R = 180+(C*(S-75))+O;
				O*=-1;
			}
			else
			{
				R = 180+(C*(S-75));
			}
		}
	}
	ret += '<div class="TH" style="transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);" ><div class="TI" style="transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';"></div></div>';
	var R = 0;
	var O = 0;
	if(E<=0 || S>=25){R = 0;}else
	{
		if(S<=0)
		{
			if(E<25)
			{
				R = -180+(C*(E));
			}
			else
			{
				R = -90;
			}
		}
		else
		{
			if(E<25)
			{
				O = ((25-E)*C);
				R = -90+(C*(S))+O;
				O*=-1;
			}
			else
			{
				R = -90+(C*(S));
			}
		}
	}
	ret += '<div class="TH" style="transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);" ><div class="TI" style="transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';"></div></div>';
	var R = 0;
	var O = 0;
	if(E<=50 || S>=75){R = -90;}else
	{ 
		if(S<=50)
		{
			if(E<75)
			{
				R = (C*(E-50));
			}
			else
			{
				R = 90;
			}
		}
		else
		{
			if(E<75)
			{
				O = ((75-E)*C);
				R = 90+(C*(S-50))+O;
				O*=-1;
			}
			else
			{
				R = 90+(C*(S-50));
			}
		}
	}
	ret += '<div class="TH" style="transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);" ><div class="TI" style="transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';"></div></div>';
	var R = 0;
	var O = 0;
	if(E<=25 || S>=50){R = -90;}else
	{
		if(S<=25)
		{
			if(E<50)
			{
				R = -90+(C*(E-25));
			}
			else
			{
				R = 0;
			}
		}
		else
		{
			if(E<50)
			{
				O = ((50-E)*C);
				R = (C*(S-25))+O;
				O*=-1;
			}
			else
			{
				R = (C*(S-25));
			}
		}
	}
	ret += '<div class="TH" style="transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);" ><div class="TI" style="transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';"></div></div>';
	ret += "</div>";
	return ret;
}
/**
 * CLine Creates a line piece
 */
Graph.prototype.CLine = function(id){
	if(this.Values[id] && this.Values[id+1]){

	}
}
/** 
 * CTag Creates a single Tag for the index
 */
Graph.prototype.CTag = function(id){
	var ret = "<div class='Tag' onmouseover='Graph.HighLight(this,"+ (id+1) +");' onmouseout='Graph.LowLight(this);'>";
	if(this.Values[id]["col"]){
		ret += "<div class='circ' style='background-color:"+ this.Values[id]["col"] +";'></div>";
	}
	ret += this.Values[id]["tag"] +"</div>";
	return ret;
}
/** 
 * HighLight 's Graph element 
 */
Graph.HighLight = function(TagE,id){
	Graph.LowLight(TagE);
	var t = TagE.parentNode.parentNode.children[0].children[id];
	t.setAttribute("class", t.getAttribute("class")+" HighLight");
}
/** 
 * LowLight resets the graph
 */
Graph.LowLight = function(TagE){
	var c = TagE.parentNode.parentNode.children[0].children;
	for(var i = 0; i < c.length; i++){
		c[i].setAttribute("class", String(c[i].getAttribute("class")).replace("HighLight",""));
	}
}

/**
 * Values based functions
 * Add and new value to the graph values
 */
Graph.prototype.Add = function(value){
	if(this.Values){
		if(this.Values[this.Values]["val"]){
			this.Values[this.Values]["val"] = value;
		} else {
			this.Values[this.Values] = value;
		}
	}
}
/**
 * Get value by id
 */
Graph.prototype.Total = function(){
	if(this.Values){
		var ret = 0;
		for(var i = 0; i<this.Values.length; i++){
			ret += this.Get(i);
		}
		return ret;
	}
}
/**
 * Get value by id
 */
Graph.prototype.Get = function(id,p){
	if(this.Values){
		if(this.Values[id]["val"]){
			if(p)
			{
				return this.Values[id]["val"]/this.Total;
			}
			return this.Values[id]["val"];
		}
		if(p)
		{
			return this.Values[id]/this.Total;
		}
		return this.Values[id];
	}
}
/**
 * Set value by id
 */
Graph.prototype.Set = function(id, value){
	if(this.Values && this.Values[id]){
		if(this.Values[id]["val"]){
			this.Values[id]["val"] = value;
		} else {
			this.Values[id] = value;
		}
	}
}