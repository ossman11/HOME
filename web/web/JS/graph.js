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

Graph.DataSet = function(tags, values, color){
	this.val = this.ParseRaw(tags, values);

	/*
	this.Itterate(this.val,
		functio
		*/
}

Graph.Color = function(r,g,b){
	this.R = r;
	this.G = g;
	this.B = b;
}

Graph.Color.prototype.toString = function(){
	return "rgb("+this.R+","+this.G+","+this.B+")";
}

Graph.Color.prototype.Fade = function(){
	this.R = this.R+parseInt((255-this.R)*0.25);
	this.G = this.G+parseInt((255-this.G)*0.25);
	this.B = this.B+parseInt((255-this.B)*0.25);
}

Graph.Color.prototype.Negative = function(){
	this.R = (this.R - 128)*-1+128;
	this.G = (this.G - 128)*-1+128;
	this.B = (this.B - 128)*-1+128;
}

Graph.DataSet.prototype.ParseRaw = function(tags, values, color){
	if(color === undefined){var color = new Graph.Color(253, 130, 41);}
	var color = new Graph.Color(color.R,color.G,color.B);
	color.Negative();color.Fade();
	var len
	if(tags.length < values.length){
		len = tags.length;
	} else {
		len = values.length;
	}

	var ret = [];
	var cur = 0;
	while(cur<len){
		ret[cur] = {};
		if(values[cur].length)
		{
			ret[cur]['tag'] = tags[cur][tags[cur].length-1];
			ret[cur]['tot'] = this.TotalChildren(values[cur]);
			ret[cur]['col'] = color.toString();
			ret[cur]['val'] = this.ParseRaw(tags[cur],values[cur],color);
			color.Fade();
		} else {
			ret[cur]['tag'] = tags[cur];
			ret[cur]['col'] = color.toString();
			ret[cur]['val'] = values[cur];
			color.Fade();
		}
		cur++;
	}
	return ret;
}

Graph.DataSet.prototype.TotalChildren = function(values){
	var ret = 0;
	if(values.length)
	{
		for(var i = 0; i < values.length; i++)
		{
			if(values[i].length){
				ret += this.TotalChildren(values[i]);
			} else {
				ret += values[i];
			}
		}
	} else {
		ret += values;
	}
	return ret;
}

Graph.DataSet.prototype.Itterate = function(val, func){

	for(var i = 0; i < val.length; i++){
		var cur = val[i];
		if(cur['val'].length){
			//console.log("Set Tag = "+cur['tag']);
			this.Itterate(cur['val'],func);
		} else {
			func(cur);
		}
	}
}

/**
 * Drawing functions
 * Draw the graph in the DOM element
 */
Graph.prototype.Draw = function(id){
	if(id === undefined){
		var val = {'val':this.Values};
		val['tot'] = this.Total; id = [];
		var title = "";
	} else {
		var tmp = this.Values[id[0]];
		var title = "<div class='Title'><div onclick='g.Draw()'> > " + tmp['tag'] + "</div>";
		var invert = [id[0]];
		for(var i = 1; i < id.length; i++){
			tmp = tmp['val'][id[i]];
			title += "<div onclick='g.Draw(["+ invert +"])'> > " + tmp['tag'];
			invert[invert.length] = id[i];
		}
		val = tmp;
	}

	var s = 0;

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
		} else if(this.Type == "CIR" || this.Type == "PIE"){
			if(this.DOM.offsetWidth < this.DOM.offsetHeight){
				s = this.DOM.offsetWidth;
			} else {
				s = this.DOM.offsetHeight;
			}
			var h = "<div class='Data' style='width:"+ s +"px;height:"+ s +"px;'><div style='width:100%;height:100%;'></div>";
			var t = "<div class='Tags'>";
			var last = 0;
			var idL = id.length;
			var pieces=[];

			for(var i = 0; i < val['val'].length; i++){
				var next = last;
				if(val['val'][i]['tot']){
					next = last+((val['val'][i]['tot']/val['tot'])*100);
					h += this.CPie(last,next,val['val'][i]['col'].toString());
					var WriteChild = function(start, par, g){
						var tmpLast = start;
						var ret = "";
						for(var x = 0; x < par['val'].length; x++){
							var tmpVar = 0;
							if(par['val'][x]['tot']){
								tmpVar = (par['val'][x]['tot']/val['tot'])*100;
								
							} else {
								tmpVar = (par['val'][x]['val']/val['tot'])*100;
							}
							ret += g.CPie(tmpLast,tmpLast+tmpVar,par['val'][x]['col'],1);
							if(par['val'][x]['tot']){ret += WriteChild(tmpLast,par['val'][x],g);}
							ret += "</div>";
							tmpLast += tmpVar;
						}
						return ret;
					}
					if(this.Type == "CIR")
					{
						h += "<div class='GCC'></div>";
					}

					var tmpLast = last;		
					h+= WriteChild(last,val['val'][i],this);					
					h += "</div>";
				} else {
					next = last+((val['val'][i]['val']/val['tot'])*100);
					h += this.CPie(last,next,val['val'][i]['col'].toString());
					if(this.Type == "CIR")
					{
						h += "<div class='GCC'></div>";
					}
					h += "</div>";
				}
				
				last = next;
				pieces[pieces.length] = last*360;
			
				id[idL] = i;				
				t += this.CTag(i, val['val'][i],id);
			}
			//h += "<div class='GC'><div class='GCC'></div></div>";
			h += "</div>";
			t += "</div>";

			this.DOM.innerHTML = t+h+title;
			this.DOM.children[1].addEventListener('mousemove',Graph.Hover.bind(null,this.Type,pieces));
			this.DOM.children[1].addEventListener('mouseout',Graph.Leave.bind(null));
			this.DOM.children[1].addEventListener('click',Graph.Click.bind(null,this.Type,pieces,id));
		}
	}
	if(s == 0){
		this.DOM.children[1].style.cssText = "width:100%;height:100%;";
	}else{
		var DelayFunc = function(size,par){
			par.children[1].style.cssText = "width:"+size+"px;height:"+size+"px;";
		}
		this.OpenData();
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
Graph.prototype.CPie = function (S,E,col,level){
	if(S==0 && E<0){S=100+E;E=100;}
	var ret = '<div class="GC" >';
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
	ret += '<div class="TH" style="/*transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);*/" ><div class="TI" style="/*transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';*/"></div></div>';
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
	ret += '<div class="TH" style="/*transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);*/" ><div class="TI" style="/*transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';*/"></div></div>';
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
	ret += '<div class="TH" style="/*transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);*/" ><div class="TI" style="/*transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';*/"></div></div>';
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
	ret += '<div class="TH" style="/*transform: rotate('+O+'deg);-ms-transform: rotate('+O+'deg);-webkit-transform: rotate('+O+'deg);*/" ><div class="TI" style="/*transform: rotate('+R+'deg);-ms-transform: rotate('+R+'deg);-webkit-transform: rotate('+R+'deg);background:'+col+';*/"></div></div>';
	// ret += "</div>";
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
Graph.prototype.CTag = function(id, val, action){
	var ret = "<div class='Tag'";
	if(val['tot']){
		ret += "onclick='Graph.CloseData(this);t=setTimeout(function(){g.Draw(["+action+"]);},256);'";
	}
	ret += " onmouseover='Graph.HighLight(this,"+ (id+1) +");' onmouseout='Graph.LowLight(this);'>";
	if(val["col"]){
		ret += "<div class='circ' style='background-color:"+ val["col"] +";'></div>";
	}
	ret += val["tag"] +"</div>";
	return ret;
}
/** 
 * HighLight 's Graph element 
 */
Graph.HighLight = function(TagE,id){
	Graph.LowLight(TagE);
	var t = TagE.parentNode.parentNode.children[1].children[id];
	t.setAttribute("class", t.getAttribute("class")+" HighLight");
}
/** 
 * LowLight resets the graph
 */
Graph.LowLight = function(TagE){
	if(TagE && TagE.parentNode && TagE.parentNode.parentNode && TagE.parentNode.parentNode.children[1])
	{
		var c = TagE.parentNode.parentNode.children[1].children;
		for(var i = 0; i < c.length; i++){
			c[i].setAttribute("class", String(c[i].getAttribute("class")).replace("HighLight",""));
		}
	}
}

Graph.CloseData = function(TagE){
	var DS = TagE.parentNode.parentNode.children[1].style;
	TagE.parentNode.parentNode.children[0].style.cssText = "height:0%;";
	DS.cssText = "margin: "+ (parseInt(DS.width)*.5) +"px auto;";
	DS.width = "0px";
	DS.height = "0px";
}

Graph.prototype.OpenData = function(){
	if(this.DOM.offsetWidth < this.DOM.offsetHeight){
		s = this.DOM.offsetWidth;
	} else {
		s = this.DOM.offsetHeight;
	}
	window.dd = this.DOM;
	var LocalOpen = function(s){
		var DOM = window.dd;
		if(DOM.offsetWidth < DOM.offsetHeight){
			s = DOM.offsetWidth;
		} else {
			s = DOM.offsetHeight;
		}
		DOM.children[1].style.cssText = "width:"+s+"px;height:"+s+"px";
		var THS = DOM.getElementsByClassName('TH');
		for(var i = 0; i < THS.length; i++){
			var tmpStyle = THS[i].attributes.getNamedItem('style');
			if(tmpStyle){
				console.log();
				THS[i].style.cssText = THS[i].attributes.getNamedItem('style').value.replace('/*','').replace('*/','');
			}
		}
		DOM.children[1].style.cssText = "width:"+s+"px;height:"+s+"px";
		var THS = DOM.getElementsByClassName('TI');
		for(var i = 0; i < THS.length; i++){
			var tmpStyle = THS[i].attributes.getNamedItem('style');
			if(tmpStyle){
				console.log();
				THS[i].style.cssText = THS[i].attributes.getNamedItem('style').value.replace('/*','').replace('*/','');
			}
		}
	}
	t = setTimeout(LocalOpen.bind(s),128);
}

Graph.Hover = function( Type, parts, evt){
	if(Type == "BAR"){

	} else if(Type == "PIE" || Type == "CIR"){
		// get all event values
		var s = evt.currentTarget.offsetWidth*.5;
		var x = (evt.x-evt.currentTarget.getBoundingClientRect().left)/s-1;
		var y = (evt.y-evt.currentTarget.getBoundingClientRect().top)/s-1;
		// calculate distance
		var d = Math.sqrt(Math.pow(y,2)+Math.pow(x,2));
		var cur = -1;
		// check wether cursor is within the circle of influence
		if(((d > 0.4 || d < -0.4) || Type == "PIE") && (d < 0.8 && d > -0.8)){
			cur = 0;
			// calculate angle
			var deg = Math.atan2(x*-1,y)/Math.PI*180-180;
			while(deg < 0){deg += 360;}
			while(deg > 360){deg -= 360;}
			// check what part fals inside this angle
			while(deg > parts[cur]/100){cur++;}		
		}
		// select all children and lowlight everything and highlight the current part
		var children = evt.currentTarget.children;
		for(var i = 0; i < children.length; i++){
			var child = children[i+1];
			if(child){
				if(i==cur){
					if(child.classList.length == 1){
						child.className += " HighLight";
					}
				} else {
					child.className = child.className.replace("HighLight","");
				}
			}
		}
	}
}

Graph.Click = function( Type, parts, id, evt){
	if(Type == "BAR"){

	} else if(Type == "PIE" || Type == "CIR"){
		// get all event values
		var s = evt.currentTarget.offsetWidth*.5;
		var x = (evt.x-evt.currentTarget.getBoundingClientRect().left)/s-1;
		var y = (evt.y-evt.currentTarget.getBoundingClientRect().top)/s-1;
		// calculate distance
		var d = Math.sqrt(Math.pow(y,2)+Math.pow(x,2));
		var cur = -1;
		// check wether cursor is within the circle of influence
		if(((d > 0.4 || d < -0.4) || Type == "PIE") && (d < 0.8 && d > -0.8)){
			cur = 0;
			// calculate angle
			var deg = Math.atan2(x*-1,y)/Math.PI*180-180;
			while(deg < 0){deg += 360;}
			while(deg > 360){deg -= 360;}
			// check what part fals inside this angle
			while(deg > parts[cur]/100){cur++;}
			var children = evt.currentTarget.children;
			if(children[cur+1].getElementsByClassName('GC').length > 0){
				if(id)
				{
					id[id.length-1] = cur;
				} else {
					id = [cur];
				}
				g.Draw(id);
			}			
		}
	}
}

Graph.Leave = function(evt){
	var children = evt.currentTarget.children;
	for(var i = 0; i < children.length; i++){
		var child = children[i+1];
		if(child){
			child.className = child.className.replace("HighLight","");
		}
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
		if(this.Values[id]["val"].length){
			if(p)
			{
				return this.Values[id]["tot"]/this.Total;
			}
			return this.Values[id]["tot"];
		}
		if(p)
		{
			return this.Values[id]['val']/this.Total;
		}
		return this.Values[id]['val'];
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

function CSS(className, name, value){
	var styles = document.styleSheets;
	for(var x = 0; x < styles.length; x++){
		var rules = styles[x].rules;
		for(var y = 0; y < rules.length; y++){
			if(rules[y].selectorText == className){
				rules[y].style[name] = value;
			}
		}
	}
}