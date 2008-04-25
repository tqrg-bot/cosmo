/*
 * Copyright 2008 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

dojo.provide("cosmo.ui.widget.SharingDialog");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.Dialog");
dojo.require("dojox.uuid");
dojo.require("cosmo.data.TicketStore");

dojo.requireLocalization("cosmo.ui.widget", "SharingDialog");

dojo.declare("cosmo.ui.widget.SharingDialog", [dijit._Widget, dijit._Templated],
{
    widgetsInTemplate: true,
    templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/SharingDialog.html'),
    l10n: dojo.i18n.getLocalization("cosmo.ui.widget", "SharingDialog"),

    // init params
    store: null,
    collection: null,
    ticketStore: null,

    // collection or subscription object
    displayName: "",
    urls: null,

    // attach points
    instructionsContainer: null,
    instructionsSelector: null,
    ticketContainer: null,
    inviteSection: null,
    readOnlyInviteLink: null,
    readWriteInviteLink: null,

    readOnlyTicket: null,
    readWriteTicket: null,

    // Functions for subscription instructions

    instructionsOnClick: function(e, instructionsKey){
        e.preventDefault();
        var instructions = dojo.string.substitute(this.l10n[instructionsKey + "Instructions"], this.urls);
        var dialog = new dijit.Dialog({title: this.l10n[instructionsKey + "InstructionsTitle"]});
        dialog.setContent(instructions);
        dialog.startup();
        dialog.show();
    },

    atomOnClick: function(e){
        this.instructionsOnClick(e, "feedReader");
    },

    chandlerOnClick: function(e){
        this.instructionsOnClick(e, "chandler");
    },

    davOnClick: function(e){
        this.instructionsOnClick(e, "dav");
    },

    iCalOnClick: function(e){
        this.instructionsOnClick(e, "iCal");
    },

    onTicket: function(ticket){
        if (!this.readOnlyTicket && (this.ticketStore.getValue(ticket, "type") == "read-only"))
            this.readOnlyTicket = ticket;
        else if (!this.readWriteTicket && (this.ticketStore.getValue(ticket, "type") == "read-write"))
            this.readWriteTicket = ticket;
    },

    inviteReadOnly: function(){
        if (this.readTicket) this.doInvite(this.readTicket);
        else {
            this.createTicket("read-only", dojo.hitch(this, this.doInvite));
        }
    },

    inviteReadWrite: function(){
        if (this.readWriteTicket) this.doInvite(this.readWriteTicket);
        else {
            this.createTicket("read-write", dojo.hitch(this, this.doInvite));
        }
    },

    getReadOnlyTicket: function(){
        if (this.readTicket){
            var d = new dojo.Deferred();
            d.callback(this.readTicket);
            return d;
        } else {
            return this.createTicket("read-only").addCallback(dojo.hitch(this, this.onTicket));
        }
    },

    getReadWriteTicket: function(){
        if (this.readTicket){
            var d = new dojo.Deferred();
            d.callback(this.readWriteTicket);
            return d;
        } else {
            return this.createTicket("read-write").addCallback(dojo.hitch(this, this.onTicket));
        }
    },

    getTwoTickets: function(){
        var rod = this.getReadOnlyTicket();
        var rwd = this.getReadWriteTicket();
        return new dojo.DeferredList([rod, rwd]);
    },

    invite: function(){
        console.log("foo");
        var d = this.getTwoTickets();
        d.addCallback(dojo.hitch(this,
            function(){
                var tickets = {
                    "read-only": this.readOnlyTicket,
                    "read-write": this.readWriteTicket
                };
                this.updateInviteLinks(tickets);
                this.showInviteLinks();
            }));
    },

    updateInviteLinks: function(tickets){
        var ro = tickets["read-only"];
        var rw = tickets["read-write"];
        var baseUrl = this.urls.html.uri;
        if (ro) this.readOnlyInviteLink.setAttribute("href", this.getTicketedUrl(baseUrl, ro));
        if (rw) this.readWriteInviteLink.setAttribute("href", this.getTicketedUrl(baseUrl, rw));
    },

    getTicketedUrl: function(url, ticket){
        var key = this.ticketStore.getValue(ticket, "key");
        return url + (url.indexOf("?") > -1? "&" : "?") + "ticket=" + key;
    },

    showInviteLinks: function(){
        dojo.style(this.inviteSection, "display", "block");
    },

    createTicket: function(type){
        var key = dojox.uuid.generateTimeBasedUuid().slice(0, 8);
        var ticket = this.ticketStore.newItem({type: type, key: key});
        var d = new dojo.Deferred();
        this.ticketStore.save({
            onComplete: function(){d.callback(ticket);},
            onError: function(e){d.errback(e);}
        });
        return d;
    },

    // lifecycle methods
    postMixInProperties: function(){
        var store = this.store;
        if (store){
            var collection = this.collection;
            this.displayName = store.getValue(collection, "displayName");
            this.urls = store.getValue(collection, "urls");
            if ((!this.ticketStore) && this.urls.ticket){
                this.ticketStore =
                    new cosmo.data.TicketStore({iri: this.urls.ticket, xhrArgs: this.xhrArgs});
            }
            dojo.addOnLoad(dojo.hitch(this, function(){
                this.tickets = this.ticketStore.fetch({
                    onItem: dojo.hitch(this, "onTicket"),
                    onError: function(e){console.debug(e);}
                });
            }));
        }
    }
});

