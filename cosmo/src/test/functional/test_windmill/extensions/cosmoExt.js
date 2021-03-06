//Author: Adam Christian, Open Source Applications Foundation
//Email: adam@osafoundation.org
//
//Description: The following function allows a test to accurately move an event DIV
//to a destination time/day using the Cosmo UI objects to calculate and compensate for
//The relative DIV offsets
//
//Since the div names are a concatonation of their hash key and a prepending string
//I will just recreate that before I actually look up the dom element, then click
//the appropriate element

windmill.controller.extensions.clickClDialog =function (param_object){
    var hash_key;
    eval ("hash_key=windmill.testWindow." + param_object.jsid + ";");
    param_object.id = "collectionSelectorItemDetails_" + hash_key;
    delete param_object.jsid;    
    return windmill.controller.click(param_object);
};

windmill.controller.extensions.clickCollection =function (param_object){
    var hash_key;
    eval ("hash_key=windmill.testWindow." + param_object.jsid + ";");
    param_object.id = "collectionSelectorItemSel_" + hash_key;
    delete param_object.jsid;
    return windmill.controller.click(param_object);
};

windmill.controller.extensions.checkCollection =function (param_object){
    var hash_key;
    eval ("hash_key=windmill.testWindow." + param_object.jsid + ";");
    param_object.id = "collectionSelectorItemCheck_" + hash_key;
    delete param_object.jsid;
    return windmill.controller.check(param_object);
};

windmill.controller.extensions.clickLozenge =function (param_object){
    var hash_key;
    // FIXME: Fixing a backwards dependency issue
    // so the tests work for both 0.2 and trunk
    var jsid = param_object.jsid.replace('windmill.testWindow.', '');
    eval ("hash_key=windmill.testWindow." + jsid + ";");
    //hash_key = eval('('+ param_object.jsid + ')');
    param_object.id = "eventDivContent__" + hash_key;
    //console.log(param_object);
    delete param_object.jsid;
    
    //Since id comes before jsid in the lookup order
    //we don't need to reset it, now go ahead and click it!
    
    return windmill.controller.click(param_object);
};
windmill.controller.extensions.clickUnTimedLozenge =function (param_object){
    var hash_key;
    // FIXME: Fixing a backwards dependency issue
    // so the tests work for both 0.2 and trunk
    var jsid = param_object.jsid.replace('windmill.testWindow.', '');
    eval ("hash_key=windmill.testWindow." + jsid + ";");
    //hash_key = eval('('+ param_object.jsid + ')');
    param_object.id = "eventDivAllDayTitle__" + hash_key;
    //console.log(param_object);
    delete param_object.jsid;
    
    //Since id comes before jsid in the lookup order
    //we don't need to reset it, now go ahead and click it!
    
    return windmill.controller.click(param_object);
};


windmill.controller.extensions.clickItem =function (param_object){
    var hash_key;
    // FIXME: Fixing a backwards dependency issue
    // so the tests work for both 0.2 and trunk
    var jsid = param_object.jsid.replace('windmill.testWindow.', '');
    eval ("hash_key=windmill.testWindow." + jsid + ";");
    //hash_key = eval('('+ param_object.jsid + ')');
    param_object.id = "listView_item" + hash_key;
    delete param_object.jsid;
    //console.log(param_object);
    //Since id comes before jsid in the lookup order
    //we don't need to reset it, now go ahead and click it!
    return windmill.controller.click(param_object);
};

windmill.controller.extensions.cosmoDragDrop = function (p){
   
    var param = p || {};
    var dragged = param.dragged;
    var dest = param.destination;
    var app = windmill.testWindow;
    // FIXME: Fixing a backwards dependency issue
    // so the tests work for both 0.2 and trunk
    var jsid = dragged.jsid.replace('windmill.testWindow.', '');

    dragged.id = dragged.pfx + eval('windmill.testWindow.' + jsid);
    // Delete the jsid to force lookup by regular id
    delete dragged.jsid;
    dragged = windmill.controller._lookupDispatch(dragged);
    dest = windmill.controller._lookupDispatch(dest);

    // Bail out if no drag elem or destination
    if (!dragged || !dest) {
        return false;
    }
    // Offsets to convert abs XY pos to local pos on canvas
    var vOff = app.TOP_MENU_HEIGHT +
        app.ALL_DAY_RESIZE_AREA_HEIGHT +
        app.DAY_LIST_DIV_HEIGHT +
        app.ALL_DAY_RESIZE_HANDLE_HEIGHT -
        app.cosmo.view.cal.canvas.getTimedCanvasScrollTop();
    var hOff = app.LEFT_SIDEBAR_WIDTH +
        app.HOUR_LISTING_WIDTH + 12;

    var mouseDownX = dragged.parentNode.offsetLeft + hOff;
    var mouseDownY = dragged.parentNode.offsetTop + vOff;

    var mouseUpX = dest.parentNode.offsetLeft + hOff;
    var mouseUpY = dest.offsetTop + vOff;

    windmill.events.triggerMouseEvent(app.document.body,
        'mousemove', true, mouseDownX, mouseDownY);
    windmill.events.triggerMouseEvent(dragged,
        'mousedown', true);
    windmill.events.triggerMouseEvent(app.document.body,
        'mousemove', true, mouseUpX, mouseUpY);
    windmill.events.triggerMouseEvent(dragged,
        'mouseup', true);
        return true;
};

windmill.registry.methods['extensions.clickClDialog']       = {'locator': true, 'option': false };
windmill.registry.methods['extensions.clickCollection']       = {'locator': true, 'option': false };
windmill.registry.methods['extensions.checkCollection']       = {'locator': true, 'option': false };
windmill.registry.methods['extensions.clickLozenge']       = {'locator': true, 'option': false };