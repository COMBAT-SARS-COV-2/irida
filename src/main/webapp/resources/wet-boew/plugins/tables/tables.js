/**
 * @title WET-BOEW Tables
 * @overview Integrates the DataTables plugin into WET providing searching, sorting, filtering, pagination and other advanced features for tables.
 * @license wet-boew.github.io/wet-boew/License-en.html / wet-boew.github.io/wet-boew/Licence-fr.html
 * @author @jeresiv
 */
(function( $, window, wb ) {
"use strict";

/*
 * Variable and function definitions.
 * These are global to the plugin - meaning that they will be initialized once per page,
 * not once per instance of plugin on the page. So, this is a good place to define
 * variables that are common to all instances of the plugin on a page.
 */
var pluginName = "wb-tables",
	selector = "." + pluginName,
	initedClass = pluginName + "-inited",
	initEvent = "wb-init" + selector,
	$document = wb.doc,
	idCount = 0,
	i18n, i18nText, defaults,

	/**
	 * Init runs once per plugin element on the page. There may be multiple elements.
	 * It will run more than once per plugin if you don't remove the selector from the timer.
	 * @method init
	 * @param {jQuery Event} event `timerpoke.wb` event that triggered the function call
	 */
	init = function( event ) {
		var elm = event.target,
			elmId = elm.id;

		// Filter out any events triggered by descendants
		// and only initialize the element once
		if ( event.currentTarget === elm &&
			elm.className.indexOf( initedClass ) === -1 ) {

			wb.remove( selector );
			elm.className += " " + initedClass;

			// Ensure there is a unique id on the element
			if ( !elmId ) {
				elmId = pluginName + "-id-" + idCount;
				idCount += 1;
				elm.id = elmId;
			}

			// Only initialize the i18nText once
			if ( !i18nText ) {
				i18n = wb.i18n;
				i18nText = {
					aria: {
						sortAscending: i18n( "sortAsc" ),
						sortDescending: i18n( "sortDesc" )
					},
					emptyTable: i18n( "emptyTbl" ),
					info: i18n( "infoEntr" ),
					infoEmpty: i18n( "infoEmpty" ),
					infoFiltered: i18n( "infoFilt" ),
					lengthMenu: i18n( "lenMenu" ),
					loadingRecords: i18n( "load" ),
					paginate: {
						first: i18n( "first" ),
						last: i18n( "last" ),
						next: i18n( "nxt" ),
						previous: i18n( "prv" )
					},
					processing: i18n( "process" ),
					search: i18n( "filter" ),
					thousands: i18n( "info1000" ),
					zeroRecords: i18n( "infoEmpty" )
				};
			}

			defaults = {
				asStripeClasses: [],
				language: i18nText,
				dom: "<'top'ilf>rt<'bottom'p><'clear'>",
				drawCallback: function() {
					$( "#" + elmId ).trigger( "tables-draw.wb" );
				}
			};

			Modernizr.load({
				load: [ "site!deps/jquery.dataTables" + wb.getMode() + ".js" ],
				complete: function() {
					var $elm = $( "#" + elmId ),
						dataTableExt = $.fn.dataTableExt;

					/*
					 * Extend sorting support
					 */
					$.extend( dataTableExt.type.order, {

						// Enable internationalization support in the sorting
						"html-pre": function( a ) {
							return wb.normalizeDiacritics(
								!a ? "" : a.replace ?
									a.replace( /<.*?>/g, "" ).toLowerCase() : a + ""
							);
						},
						"string-case-pre": function( a ) {
							return wb.normalizeDiacritics( a );
						},
						"string-pre": function( a ) {
							return wb.normalizeDiacritics( a );
						},

						// Formatted number sorting
						"formatted-num-asc": function( a, b ) {
							return wb.formattedNumCompare( b, a );
						},
						"formatted-num-desc": function( a, b ) {
							return wb.formattedNumCompare( a, b );
						}
					} );

					/*
					 * Extend type detection
					 */
					// Formatted numbers detection
					// Based on: http://datatables.net/plug-ins/type-detection#formatted_numbers
					dataTableExt.aTypes.unshift(
						function( sData ) {

							// Strip off HTML tags and all non-alpha-numeric characters (except minus sign)
							var deformatted = sData.replace( /<[^>]*>/g, "" ).replace( /[^\d\-\/a-zA-Z]/g, "" );
							if ( $.isNumeric( deformatted ) || deformatted === "-" ) {
								return "formatted-num";
							}
							return null;
						}
					);

					// Add the container or the sorting icons
					$elm.find( "th" ).append( "<span class='sorting-cnt'><span class='sorting-icons'></span></span>" );

					// Create the DataTable object
					$elm.dataTable( $.extend( true, {}, defaults, window[ pluginName ], wb.getData( $elm, pluginName ) ) );
				}
			});
		}
	};

// Bind the init event of the plugin
$document.on( "timerpoke.wb " + initEvent, selector, init );

// Add the timer poke to initialize the plugin
wb.add( selector );

})( jQuery, window, wb );
