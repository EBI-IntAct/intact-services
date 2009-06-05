//------------------------------------------------------------------------------------------	    
// CREATE NON POSITIONAL FEATURE TABLE
//------------------------------------------------------------------------------------------	
	function createNPFeatureTable(featureXML_num, array, tagId, npf_num)
	{

	if(npf_num == 0)
	 	{
	  // There are not positional features
	  	}
	 else
	 	{		
			var title_var = non_positional_features_coulmns;

			/**
			* The first time that a DAS source is loaded:
			*/
			var countNPF = dasty2.countNPF
			if(countNPF == 0)
				{
					dasty2.countNPF++;
					//Principio
					var title = new Array();
					title["type_category"] = "EVIDENCE (Category)";
					title["type_data"] = "TYPE NAME";
					title["type_id"] = "FEATURE TYPE";
					title["feature_id"] = "FEATURE ID";
					title["feature_label"] = "LABEL";
					title["note_data"] = "NOTE";
					title["method_data"] = "METHOD";
					title["score_data"] = "S.";
					title["annotation_server"] = "SERVER NAME";
					title["link_data"] = "";
					title["version"] = "";
					
					var title_width = new Array();
					title_width["type_category"] = col_category_width;
					title_width["type_data"] = col_type_width;
					//title_width["type_id"] = col_type_width;
					title_width["type_id"] = 120;
					title_width["feature_label"] = col_id_width;
					title_width["note_data"] = "";
					title_width["method_data"] = "";
					title_width["score_data"] = 40;
					title_width["annotation_server"] = col_server_width;
					title_width["link_data"] = 5;
					title_width["version"] = 5;
	
					
					var mybody = document.getElementById(tagId);
					var mytable = document.createElement("table");
					mytable.setAttribute("id","non_positional_features");
					mytable.setAttribute("class","sortable feature_table");
					mytable.setAttribute("className","sortable feature_table");
					
					if (typeof non_positional_feature_table_width != "undefined")
						mytable.style.cssText = "width:" + non_positional_feature_table_width + ";";
				
					//mytable.setAttribute("class","sortable");
					//mytable.setAttribute("className","sortable");
					
					//var mythead = document.createElement('thead');
					
					var mytbody = document.createElement('tbody');
					mytbody.setAttribute("id","non_positional_features_tbody");
					
					var mycurrent_row = document.createElement("tr");
					mycurrent_row.setAttribute("class", "feature_table_row_title_decor");
					mycurrent_row.setAttribute("className", "feature_table_row_title_decor");
					for(var i = 0; i < title_var.length; i++)
					  {
						  var mycurrent_cell = document.createElement("th");
						  if(title_var[i] == "note_data" || title_var[i] == "link_data" || title_var[i] == "version")
						  	{
								mycurrent_cell.setAttribute("class", "feature_table_cell_title_decor" + i + " unsortable");
						 		mycurrent_cell.setAttribute("className", "feature_table_cell_title_decor" + i + " unsortable");
							}
						  else
						  	{
								mycurrent_cell.setAttribute("class", "feature_table_cell_title_decor" + i);
						  		mycurrent_cell.setAttribute("className", "feature_table_cell_title_decor" + i);
							}
							
						  if(title_width[title_var[i]] != "")
						  	{
						  		mycurrent_cell.style.cssText = "width:" + title_width[title_var[i]] + "px;";
							}
						  var content = document.createTextNode(title[title_var[i]]);
						  mycurrent_cell.appendChild(content);
						  mycurrent_row.appendChild(mycurrent_cell);
					  }
					  
					//mythead.appendChild(mycurrent_row);
					mytbody.appendChild(mycurrent_row);
					//mytable.appendChild(mythead);
					mytable.appendChild(mytbody);
					mybody.appendChild(mytable);
				}

						// Lo del medio

			var mytbody = document.getElementById("non_positional_features_tbody");
			var npfTypes = new Array();
			var newType = false;

			
			for(var j = 0; j < array.length; j++)
				{	
					  if (array[j]["start_data"]==0 && array[j]["end_data"]==0)
						 { // non positional feature	
						 
							newType = false;
							
							if(typeof npfTypes[array[j]["type_id"]] == "undefined")
								{
									newType = true;
								}
								
							if(newType == true)
								{
									npfTypes[array[j]["type_id"]] = 0;
								}
							else
								{
									npfTypes[array[j]["type_id"]]++;
								}
					
	
							var mycurrent_row = document.createElement("tr");
							
							var tr_name = "npf_item" + featureXML_num + "_" + array[j]["type_id"] + "_" + npfTypes[array[j]["type_id"]];
							var tr_id_name = [];
							tr_id_name.push(tr_name);
							
							dasty2.IdlinesPerType.push([array[j]["type_id"], tr_id_name, 1]);
							dasty2.IdlinesPerCategory.push([array[j]["type_category"], tr_id_name, 1]);
							dasty2.IdlinesPerServer.push([array[j]["annotation_server"], tr_id_name, 1]);	
							
							
							
					  		mycurrent_row.setAttribute("id", tr_id_name);
							mycurrent_row.style.cssText = "display: table-row;"; //display: block;
							
							if(dasty2.decor_tr_npf == 0)
								{
									mycurrent_row.setAttribute("class", "feature_table_row_decor0");
									mycurrent_row.setAttribute("className", "feature_table_row_decor0");
									dasty2.decor_tr_npf++;
								}
							else
								{
									mycurrent_row.setAttribute("class", "feature_table_row_decor1");
									mycurrent_row.setAttribute("className", "feature_table_row_decor1");
									dasty2.decor_tr_npf--;
								}
									  
							for(var i = 0; i < title_var.length; i++)
								{
									var mycurrent_cell = document.createElement("td");
									//mycurrent_cell.setAttribute("class", "featuretable_cell_decor" + i);
									//mycurrent_cell.setAttribute("className", "featuretable_cell_decor" + i);
									
									if(title_var[i] == "link_data")
										{
											for(var w = 0; w < array[j][title_var[i]].length; w++)
												{
													var content = document.createElement("img");
													content.setAttribute("src", "img/ico_info2.gif");
													content.setAttribute("alt", array[j][title_var[i]][w]);
													content.setAttribute("border","0");
													
													var link_content = document.createElement("a");
													link_content.setAttribute("target", "_featurelinks");
													link_content.setAttribute("href", array[j]["link_href"][w]);
													link_content.appendChild(content);	

													mycurrent_cell.appendChild(link_content);
												}
										}
									else if(title_var[i] == "version")
										{
											var content = document.createElement("img");
											content.setAttribute("border","0");
											if(annotation_version[featureXML_num] == 0){
												content.setAttribute("src", "img/checkmark.gif");	
											} else {
												content.setAttribute("src", "img/warning.gif");	
											}
											mycurrent_cell.appendChild(content);
													//var content = document.createElement("img");
													content.setAttribute("src", "img/checkmark.gif");
													//content.setAttribute("alt", array[j][title_var[i]][w]);
													content.setAttribute("border","0");
													
													//var link_content = document.createElement("a");
													//link_content.setAttribute("target", "_version");
													//link_content.setAttribute("href", array[j]["link_href"][w]);
													//link_content.appendChild(content);	

											
										}
									else if(title_var[i] == "note_data")
										{
											
											for(var w = 0; w < array[j][title_var[i]].length; w++)
												{
													var content = document.createTextNode(array[j][title_var[i]][w]);
													mycurrent_cell.appendChild(content);
													var br = document.createElement("br");
													mycurrent_cell.appendChild(br);
												}
										}
									else
										{
											if (title_var[i] == "type_category") 
											{
												/* OMAR's CODE: OLS LINK FOR NPF */
												
												var catOnto = array[j][title_var[i]];	
												var catOntoId = "";
										
												/* Separate the category ID and term */
												var catAux = catOnto.split('(');
												if (catAux[1] != null )
												{
													var catAux2 = catAux[1].split(')');
													catOntoId = catAux2[0].replace(/^\s+|\s+$/gi, ""); // trim function
													
													if (catAux[0] != null)
														catOnto = catAux[0];
												}

												/* If the category is not a known ontology, we do not add OLS URL */
												if(catOntoId)
												{
													var content = document.createTextNode(catOnto);
													
													var mycurrent_category_text = document.createElement("a");
													mycurrent_category_text.setAttribute("class", "gr_text_01");
													mycurrent_category_text.setAttribute("className", "gr_text_01");
													mycurrent_category_text.setAttribute("target", "_type");
													mycurrent_category_text.setAttribute("href", "http://www.ebi.ac.uk/ontology-lookup/?termId=" + catOntoId);
													
													mycurrent_category_text.appendChild(content);
													mycurrent_cell.appendChild(mycurrent_category_text);
												}
												else
												{
													var content = document.createTextNode(catOnto);
													mycurrent_cell.appendChild(content);
												}
												/* \OMAR's CODE */
											}
											else 
											{
												if (title_var[i] == "annotation_server") {
													var serverName = array[j][title_var[i]];
													var content = document.createTextNode(serverName);
													
													var serverRegistryURI = "";
													
													/* Search Registry URI, there are not other way ?? */
													for (var w = 0; w < feature_url.length; w++) {
														if (feature_url[w].id == serverName) {
															serverRegistryURI = feature_url[w].registry_uri;
															break;
														}
													}
													
													var mycurrent_server_text = document.createElement("a");
													mycurrent_server_text.setAttribute("class", "gr_text_01");
													mycurrent_server_text.setAttribute("className", "gr_text_01");
													mycurrent_server_text.setAttribute("target", "_dasregistry")
													mycurrent_server_text.setAttribute("href", "http://www.dasregistry.org/showdetails.jsp?auto_id=" + serverRegistryURI);
													
													mycurrent_server_text.appendChild(content);
													mycurrent_cell.appendChild(mycurrent_server_text);
													
												}
												else {
												
													if (title_var[i] == "type_id") 
													{
														var typeId = array[j][title_var[i]];
														var typeTerm = array[j]['type_data'];
														var content = document.createTextNode(typeTerm.replace(/_/g, " "));
														
														var mycurrent_type_text = document.createElement("a");
														mycurrent_type_text.setAttribute("class", "gr_text_01");
														mycurrent_type_text.setAttribute("className", "gr_text_01");
														mycurrent_type_text.setAttribute("target", "_type")
														mycurrent_type_text.setAttribute("href", "http://www.ebi.ac.uk/ontology-lookup/?termId=" + typeId);
														
														mycurrent_type_text.appendChild(content);
														mycurrent_cell.appendChild(mycurrent_type_text);
													}
													else 
													{
														var content = document.createTextNode(array[j][title_var[i]]);
														mycurrent_cell.appendChild(content);
													}
												}
											}
										}
									
									//title_width["link_data"] = 5;
					
					//title["link_href"] = "LINK";
					
					
					
									
									mycurrent_row.appendChild(mycurrent_cell);
								}
							mytbody.appendChild(mycurrent_row);
						 } // if (array[j]["start_data"]==0 && array[j]["end_data"]==0)
					
					
				
					
					
				} // for(var j = 0; j < array.length; j++)
			
	


		
		
		
	    } //if(npf_num == 0)
	    
		/* Set the display mode of each column after each request: in case user cancels Dasty2 */
		if (!show_col_category_npf) { 	toggleColumnNPF("type_category", false); setCheckboxOptionsNPF("menu_mo_img_category_column_npf", false); }
		if (!show_col_type_npf) {  		toggleColumnNPF("type_id", false);		setCheckboxOptionsNPF("menu_mo_img_type_column_npf", false); }
		if (!show_col_method_npf) {		toggleColumnNPF("method_data", false); 	setCheckboxOptionsNPF("menu_mo_img_method_column_npf", false);	}
		if (!show_col_label_npf) {		toggleColumnNPF("feature_label", false);	setCheckboxOptionsNPF("menu_mo_img_label_column_npf", false); }
		if (!show_col_note_npf ) 	{	toggleColumnNPF ( "note_data", false);	setCheckboxOptionsNPF("menu_mo_img_note_column_npf", false);}
		if (!show_col_score_npf ) 	{	toggleColumnNPF ( "score_data", false);	setCheckboxOptionsNPF("menu_mo_img_score_column_npf", false);}
		if (!show_col_server_npf) {		toggleColumnNPF("annotation_server", false);	setCheckboxOptionsNPF("menu_mo_img_server_column_npf", false); }
		if (!show_col_featureid_npf) {	toggleColumnNPF("feature_id", false);	setCheckboxOptionsNPF("menu_mo_img_featureid_column_npf", false); }
    }
	
	