//Author      : @arboshiki
//create lobibox object
var Lobibox = Lobibox || {};
(function () {

    Lobibox.counter = 0;
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

    //User can set default properties for prompt in the following way
    //Lobibox.prompt.DEFAULT_OPTIONS = object;
    Lobibox.prompt = function (type, options) {
        return new LobiboxPrompt(type, options);
    };
    //User can set default properties for confirm in the following way
    //Lobibox.confirm.DEFAULT_OPTIONS = object;
    Lobibox.confirm = function (options) {
        return new LobiboxConfirm(options);
    };
    //User can set default properties for progress in the following way
    //Lobibox.progress.DEFAULT_OPTIONS = object;
    Lobibox.progress = function (options) {
        return new LobiboxProgress(options);
    };
    //Create empty objects in order user to be able to set default options in the following way
    //Lobibox.error.DEFAULT_OPTIONS = object;
    //Lobibox.success.DEFAULT_OPTIONS = object;
    //Lobibox.warning.DEFAULT_OPTIONS = object;
    //Lobibox.info.DEFAULT_OPTIONS = object;

    Lobibox.error = {};
    Lobibox.success = {};
    Lobibox.warning = {};
    Lobibox.info = {};

    //User can set default properties for alert in the following way
    //Lobibox.alert.DEFAULT_OPTIONS = object;
    Lobibox.alert = function (type, options) {
        if (["success", "error", "warning", "info"].indexOf(type) > -1) {
            return new LobiboxAlert(type, options);
        }
    };
    //User can set default properties for window in the following way
    //Lobibox.window.DEFAULT_OPTIONS = object;
    Lobibox.window = function (options) {
        return new LobiboxWindow('window', options);
    };


    /**
     * Base prototype for all messageboxes and window
     */
    var LobiboxBase = {
        $type: null,
        $el: null,
        $options: null,
        debug: function () {
            if (this.$options.debug) {
                window.console.debug.apply(window.console, arguments);
            }
        },
        _processInput: function (options) {
            if ($.isArray(options.buttons)) {
                var btns = {};
                for (var i = 0; i < options.buttons.length; i++) {
                    btns[options.buttons[i]] = Lobibox.base.OPTIONS.buttons[options.buttons[i]];
                }
                options.buttons = btns;
            }
            options.customBtnClass = options.customBtnClass ? options.customBtnClass : Lobibox.base.DEFAULTS.customBtnClass;
            for (var i in options.buttons) {
                if (options.buttons.hasOwnProperty(i)) {
                    var btn = options.buttons[i];
                    btn = $.extend({}, Lobibox.base.OPTIONS.buttons[i], btn);
                    if (!btn['class']) {
                        btn['class'] = options.customBtnClass;
                    }
                    options.buttons[i] = btn;
                }
            }
            options = $.extend({}, Lobibox.base.DEFAULTS, options);
            if (options.showClass === undefined) {
                options.showClass = Lobibox.base.OPTIONS.showClass;
            }
            if (options.hideClass === undefined) {
                options.hideClass = Lobibox.base.OPTIONS.hideClass;
            }
            if (options.baseClass === undefined) {
                options.baseClass = Lobibox.base.OPTIONS.baseClass;
            }
            if (options.delayToRemove === undefined) {
                options.delayToRemove = Lobibox.base.OPTIONS.delayToRemove;
            }
            if (!options.iconClass) {
                options.iconClass = Lobibox.base.OPTIONS.icons[options.iconSource][this.$type];
            }
            return options;
        },
        _init: function () {
            var me = this;

            me._createMarkup();
            me.setTitle(me.$options.title);
            if (me.$options.draggable && !me._isMobileScreen()) {
                me.$el.addClass('draggable');
                me._enableDrag();
            }
            if (me.$options.closeButton) {
                me._addCloseButton();
            }
            if (me.$options.closeOnEsc) {
                $(document).on('keyup.lobibox', function (ev) {
                    if (ev.which === 27) {
                        me.destroy();
                    }
                });
            }
            if (me.$options.baseClass) {
                me.$el.addClass(me.$options.baseClass);
            }
            if (me.$options.showClass) {
                me.$el.removeClass(me.$options.hideClass);
                me.$el.addClass(me.$options.showClass);
            }
            me.$el.data('lobibox', me);
        },

        /**
         * Calculate top, left position based on string keyword
         *
         * @param {string} position "'top', 'center', 'bottom'"
         * @returns {{left: number, top: number}}
         * @private
         */
        _calculatePosition: function (position) {
            var me = this;
            var top;
            if (position === 'top') {
                top = 30;
            } else if (position === 'bottom') {
                top = $(window).outerHeight() - me.$el.outerHeight() - 30;
            } else {
                top = ($(window).outerHeight() - me.$el.outerHeight()) / 2;
            }
            var left = ($(window).outerWidth() - me.$el.outerWidth()) / 2;
            return {
                left: left,
                top: top
            };
        },

        _createButton: function (type, op) {
            var me = this;
            var btn = $('<button></button>')
                .addClass(op['class'])
                .attr('data-type', type)
                .html(op.text);
            if (me.$options.callback && typeof me.$options.callback === 'function') {
                btn.on('click.lobibox', function (ev) {
                    var bt = $(this);
                    me._onButtonClick(me.$options.buttons[type], type);
                    me.$options.callback(me, bt.data('type'), ev);
                });
            }
            btn.click(function () {
                me._onButtonClick(me.$options.buttons[type], type);
            });
            return btn;
        },

        _onButtonClick: function (buttonOptions, type) {
            var me = this;

            if ((type === 'ok' && me.$type === 'prompt' && me.isValid() || me.$type !== 'prompt' || type !== 'ok')
                && buttonOptions && buttonOptions.closeOnClick) {
                me.destroy();
            }
        },

        _generateButtons: function () {
            var me = this;
            var btns = [];
            for (var i in me.$options.buttons) {
                if (me.$options.buttons.hasOwnProperty(i)) {
                    var op = me.$options.buttons[i];
                    var btn = me._createButton(i, op);
                    btns.push(btn);
                }
            }
            return btns;
        },
        _createMarkup: function () {
            var me = this;
            var lobibox = $('<div class="lobibox"></div>');
            lobibox.attr('data-is-modal', me.$options.modal);
            var header = $('<div class="lobibox-header"></div>')
                .append('<span class="lobibox-title"></span>')
            ;
            var body = $('<div class="lobibox-body"></div>');
            lobibox.append(header);
            lobibox.append(body);
            if (me.$options.buttons && !$.isEmptyObject(me.$options.buttons)) {
                var footer = $('<div class="lobibox-footer"></div>');
                footer.append(me._generateButtons());
                lobibox.append(footer);
                if (Lobibox.base.OPTIONS.buttonsAlign.indexOf(me.$options.buttonsAlign) > -1) {
                    footer.addClass('text-' + me.$options.buttonsAlign);
                }
            }
            me.$el = lobibox
                .addClass(Lobibox.base.OPTIONS.modalClasses[me.$type])
            ;
        },
        _setSize: function () {
            var me = this;
            me.setWidth(me.$options.width);
            if (me.$options.height === 'auto') {
                me.setHeight(me.$el.outerHeight());
            } else {
                me.setHeight(me.$options.height);
            }
        },
        _calculateBodyHeight: function (height) {
            var me = this;
            var headerHeight = me.$el.find('.lobibox-header').outerHeight();
            var footerHeight = me.$el.find('.lobibox-footer').outerHeight();
            return height - (headerHeight ? headerHeight : 0) - (footerHeight ? footerHeight : 0);

        },

        /**
         * Add backdrop in case if backdrop does not exist
         *
         * @private
         */
        _addBackdrop: function () {
            if ($('.lobibox-backdrop').length === 0) {
                $('body').append('<div class="lobibox-backdrop"></div>');
            }
        },

        _triggerEvent: function (type) {
            var me = this;
            if (me.$options[type] && typeof me.$options[type] === 'function') {
                me.$options[type](me);
            }
        },

        _calculateWidth: function (width) {
            var me = this;
            width = Math.min(Math.max(width, me.$options.width), $(window).outerWidth());
            if (width === $(window).outerWidth()) {
                width -= 2 * me.$options.horizontalOffset;
            }
            return width;
        },

        _calculateHeight: function (height) {
            var me = this;
            console.log(me.$options.height);
            height = Math.min(Math.max(height, me.$options.height), $(window).outerHeight());
            if (height === $(window).outerHeight()) {
                height -= 2 * me.$options.verticalOffset;
            }
            return height;
        },

        _addCloseButton: function () {
            var me = this;
            var closeBtn = $('<span class="btn-close">&times;</span>');
            me.$el.find('.lobibox-header').append(closeBtn);
            closeBtn.on('mousedown', function (ev) {
                ev.stopPropagation();
            });
            closeBtn.on('click.lobibox', function () {
                me.destroy();
            });
        },
        _position: function () {
            var me = this;

            me._setSize();
            var pos = me._calculatePosition();
            me.setPosition(pos.left, pos.top);
        },
        _isMobileScreen: function () {
            return $(window).outerWidth() < 768;
        },
        _enableDrag: function () {
            var el = this.$el,
                heading = el.find('.lobibox-header');

            heading.on('mousedown.lobibox', function (ev) {
                el.attr('offset-left', ev.offsetX);
                el.attr('offset-top', ev.offsetY);
                el.attr('allow-drag', 'true');
            });
            $(document).on('mouseup.lobibox', function () {
                el.attr('allow-drag', 'false');
            });
            $(document).on('mousemove.lobibox', function (ev) {
                if (el.attr('allow-drag') === 'true') {
                    var left = ev.clientX - parseInt(el.attr('offset-left'), 10) - parseInt(el.css('border-left-width'), 10);
                    var top = ev.clientY - parseInt(el.attr('offset-top'), 10) - parseInt(el.css('border-top-width'), 10);
                    el.css({
                        left: left,
                        top: top
                    });
                }
            });
        },

        /**
         * Set the message of messagebox
         *
         * @param {string} msg "new message of messagebox"
         * @returns {LobiboxBase}
         * @private
         */
        _setContent: function (msg) {
            var me = this;
            me.$el.find('.lobibox-body').html(msg);
            return me;
        },

        _beforeShow: function () {
            var me = this;
            me._triggerEvent('onShow');
        },

        _afterShow: function () {
            var me = this;
            Lobibox.counter++;
            me.$el.attr('data-nth', Lobibox.counter);
            if (!me.$options.draggable) {
                $(window).on('resize.lobibox-' + me.$el.attr('data-nth'), function () {
                    me.refreshWidth();
                    me.refreshHeight();
                    me.$el.css('left', '50%').css('margin-left', '-' + (me.$el.width() / 2) + 'px');
                    me.$el.css('top', '50%').css('margin-top', '-' + (me.$el.height() / 2) + 'px');
                });
            }

            me._triggerEvent('shown');
        },

        _beforeClose: function () {
            var me = this;
            me._triggerEvent('beforeClose');
        },

        _afterClose: function () {
            var me = this;
            if (!me.$options.draggable) {
                $(window).off('resize.lobibox-' + me.$el.attr('data-nth'));
            }
            me._triggerEvent('closed');
        },
//------------------------------------------------------------------------------
//--------------------------PUBLIC METHODS--------------------------------------
//------------------------------------------------------------------------------

        /**
         * Hide the messagebox
         *
         * @returns {LobiboxBase}
         */
        hide: function () {
            var me = this;
            if (me.$options.hideClass) {
                me.$el.removeClass(me.$options.showClass);
                me.$el.addClass(me.$options.hideClass);
                setTimeout(function () {
                    callback();
                }, me.$options.delayToRemove);
            } else {
                callback();
            }

            function callback() {
                me.$el.addClass('lobibox-hidden');
                if ($('.lobibox[data-is-modal=true]:not(.lobibox-hidden)').length === 0) {
                    $('.lobibox-backdrop').remove();
                    $('body').removeClass(Lobibox.base.OPTIONS.bodyClass);
                }
            }

            return this;
        },

        /**
         * Removes the messagebox from document
         *
         * @returns {LobiboxBase}
         */
        destroy: function () {
            var me = this;
            me._beforeClose();
            if (me.$options.hideClass) {
                me.$el.removeClass(me.$options.showClass).addClass(me.$options.hideClass);
                setTimeout(function () {
                    callback();
                }, me.$options.delayToRemove);
            } else {
                callback();
            }

            function callback() {
                me.$el.remove();
                if ($('.lobibox[data-is-modal=true]').length === 0) {
                    $('.lobibox-backdrop').remove();
                    $('body').removeClass(Lobibox.base.OPTIONS.bodyClass);
                }
                me._afterClose();
            }

            return this;
        },

        /**
         * Set the width of messagebox
         *
         * @param {number} width "new width of messagebox"
         * @returns {LobiboxBase}
         */
        setWidth: function (width) {
            var me = this;
            me.$el.css('width', me._calculateWidth(width));
            return me;
        },

        refreshWidth: function () {
            this.setWidth(this.$el.width());
        },

        refreshHeight: function () {
            this.setHeight(this.$el.height());
        },

        /**
         * Set the height of messagebox
         *
         * @param {number} height "new height of messagebox"
         * @returns {LobiboxBase}
         */
        setHeight: function (height) {
            var me = this;
            me.$el.css('height', me._calculateHeight(height))
                .find('.lobibox-body')
                .css('height', me._calculateBodyHeight(me.$el.innerHeight()));
            return me;
        },

        /**
         * Set the width and height of messagebox
         *
         * @param {number} width "new width of messagebox"
         * @param {number} height "new height of messagebox"
         * @returns {LobiboxBase}
         */
        setSize: function (width, height) {
            var me = this;
            me.setWidth(width);
            me.setHeight(height);
            return me;
        },

        /**
         * Set position of messagebox
         *
         * @param {number|String} left "left coordinate of messsagebox or string representaing position. Available: ('top', 'center', 'bottom')"
         * @param {number} top
         * @returns {LobiboxBase}
         */
        setPosition: function (left, top) {
            var pos;
            if (typeof left === 'number' && typeof top === 'number') {
                pos = {
                    left: left,
                    top: top
                };
            } else if (typeof left === 'string') {
                pos = this._calculatePosition(left);
            }
            this.$el.css(pos);
            return this;
        },
        /**
         * Set the title of messagebox
         *
         * @param {string} title "new title of messagebox"
         * @returns {LobiboxBase}
         */
        setTitle: function (title) {
            return this.$el.find('.lobibox-title').html(title);
        },

        /**
         * Get the title of messagebox
         *
         * @returns {string}
         */
        getTitle: function () {
            return this.$el.find('.lobibox-title').html();
        },

        /**
         * Show messagebox
         *
         * @returns {LobiboxBase}
         */
        show: function () {
            var me = this,
                $body = $('body');

            me._beforeShow();

            me.$el.removeClass('lobibox-hidden');
            $body.append(me.$el);
            if (me.$options.buttons) {
                var buttons = me.$el.find('.lobibox-footer').children();
                buttons[0].focus();
            }
            if (me.$options.modal) {
                $body.addClass(Lobibox.base.OPTIONS.bodyClass);
                me._addBackdrop();
            }
            if (me.$options.delay !== false) {
                setTimeout(function () {
                    me.destroy();
                }, me.$options.delay);
            }
            me._afterShow();
            return me;
        }
    };
    //User can set default options by this variable
    Lobibox.base = {};
    Lobibox.base.OPTIONS = {
        bodyClass: 'lobibox-open',

        modalClasses: {
            'error': 'lobibox-error',
            'success': 'lobibox-success',
            'info': 'lobibox-info',
            'warning': 'lobibox-warning',
            'confirm': 'lobibox-confirm',
            'progress': 'lobibox-progress',
            'prompt': 'lobibox-prompt',
            'default': 'lobibox-default',
            'window': 'lobibox-window'
        },
        buttonsAlign: ['left', 'center', 'right'],
        buttons: {
            ok: {
                'class': 'lobibox-btn lobibox-btn-default',
                text: 'OK',
                closeOnClick: true
            },
            cancel: {
                'class': 'lobibox-btn lobibox-btn-cancel',
                text: 'Cancel',
                closeOnClick: true
            },
            yes: {
                'class': 'lobibox-btn lobibox-btn-yes',
                text: 'Yes',
                closeOnClick: true
            },
            no: {
                'class': 'lobibox-btn lobibox-btn-no',
                text: 'No',
                closeOnClick: true
            }
        },
        icons: {
            bootstrap: {
                confirm: 'glyphicon glyphicon-question-sign',
                success: 'glyphicon glyphicon-ok-sign',
                error: 'glyphicon glyphicon-remove-sign',
                warning: 'glyphicon glyphicon-exclamation-sign',
                info: 'glyphicon glyphicon-info-sign'
            },
            fontAwesome: {
                confirm: 'fa fa-question-circle',
                success: 'fa fa-check-circle',
                error: 'fa fa-times-circle',
                warning: 'fa fa-exclamation-circle',
                info: 'fa fa-info-circle'
            }
        }
    };
    Lobibox.base.DEFAULTS = {
        horizontalOffset: 5,                //If the messagebox is larger (in width) than window's width. The messagebox's width is reduced to window width - 2 * horizontalOffset
        verticalOffset: 5,                  //If the messagebox is larger (in height) than window's height. The messagebox's height is reduced to window height - 2 * verticalOffset
        width: 600,
        height: 'auto',                     // Height is automatically calculated by width
        closeButton: true,                  // Show close button or not
        draggable: false,                   // Make messagebox draggable
        customBtnClass: 'lobibox-btn lobibox-btn-default', // Class for custom buttons
        modal: true,
        debug: false,
        buttonsAlign: 'center',             // Position where buttons should be aligned
        closeOnEsc: true,                   // Close messagebox on Esc press
        delayToRemove: 200,                 // Time after which lobibox will be removed after remove call. (This option is for hide animation to finish)
        delay: false,                       // Time to remove lobibox after shown
        baseClass: 'animated-super-fast',   // Base class to add all messageboxes
        showClass: 'zoomIn',                // Show animation class
        hideClass: 'zoomOut',               // Hide animation class
        iconSource: 'bootstrap',            // "bootstrap" or "fontAwesome" the library which will be used for icons

        //events
        //When messagebox show is called but before it is actually shown
        onShow: null,
        //After messagebox is shown
        shown: null,
        //When messagebox remove method is called but before it is actually hidden
        beforeClose: null,
        //After messagebox is hidden
        closed: null
    };
//------------------------------------------------------------------------------
//-------------------------LobiboxPrompt----------------------------------------
//------------------------------------------------------------------------------
    function LobiboxPrompt(type, options) {
        this.$input = null;
        this.$type = 'prompt';
        this.$promptType = type;

        options = $.extend({}, Lobibox.prompt.DEFAULT_OPTIONS, options);

        this.$options = this._processInput(options);

        this._init();
        this.debug(this);
    }

    LobiboxPrompt.prototype = $.extend({}, LobiboxBase, {
        constructor: LobiboxPrompt,

        _processInput: function (options) {
            var me = this;

            var mergedOptions = LobiboxBase._processInput.call(me, options);
            mergedOptions.buttons = {
                ok: Lobibox.base.OPTIONS.buttons.ok,
                cancel: Lobibox.base.OPTIONS.buttons.cancel
            };
            options = $.extend({}, mergedOptions, LobiboxPrompt.DEFAULT_OPTIONS, options);
            return options;
        },

        _init: function () {
            var me = this;
            LobiboxBase._init.call(me);
            me.show();
        },

        _afterShow: function () {
            var me = this;
            me._setContent(me._createInput())._position();
            me.$input.focus();
            LobiboxBase._afterShow.call(me);
        },

        _createInput: function () {
            var me = this,
                label;
            if (me.$options.multiline) {
                me.$input = $('<textarea></textarea>').attr('rows', me.$options.lines);
            } else {
                me.$input = $('<input type="' + me.$promptType + '"/>');
            }
            me.$input.addClass('lobibox-input').attr(me.$options.attrs);
            if (me.$options.value) {
                me.setValue(me.$options.value);
            }
            if (me.$options.label) {
                label = $('<label>' + me.$options.label + '</label>');
            }
            return $('<div></div>').append(label, me.$input);
        },

        /**
         * Set value of input
         *
         * @param {string} val "value of input"
         * @returns {LobiboxPrompt}
         */
        setValue: function (val) {
            this.$input.val(val);
            return this;
        },

        /**
         * Get value of input
         *
         * @returns {String}
         */
        getValue: function () {
            return this.$input.val();
        },

        isValid: function () {
            var me = this,
                $error = me.$el.find('.lobibox-input-error-message');

            if (me.$options.required && !me.getValue()) {
                me.$input.addClass('invalid');
                if ($error.length === 0) {
                    me.$el.find('.lobibox-body').append('<p class="lobibox-input-error-message">' + me.$options.errorMessage + '</p>');
                    me._position();
                    me.$input.focus();
                }
                return false;
            }
            me.$input.removeClass('invalid');
            $error.remove();
            me._position();
            me.$input.focus();

            return true;
        }
    });

    LobiboxPrompt.DEFAULT_OPTIONS = {
        width: 400,
        attrs: {},          // Object of any valid attribute of input field
        value: '',          // Value which is given to textfield when messagebox is created
        multiline: false,   // Set this true for multiline prompt
        lines: 3,           // This works only for multiline prompt. Number of lines
        type: 'text',       // Prompt type. Available types (text|number|color)
        label: '',          // Set some text which will be shown exactly on top of textfield
        required: true,
        errorMessage: 'The field is required'
    };
//------------------------------------------------------------------------------
//-------------------------LobiboxConfirm---------------------------------------
//------------------------------------------------------------------------------
    function LobiboxConfirm(options) {
        this.$type = 'confirm';

//        options = $.extend({}, Lobibox.confirm.DEFAULT_OPTIONS, options);

        this.$options = this._processInput(options);
        this._init();
        this.debug(this);
    }

    LobiboxConfirm.prototype = $.extend({}, LobiboxBase, {
        constructor: LobiboxConfirm,

        _processInput: function (options) {
            var me = this;

            var mergedOptions = LobiboxBase._processInput.call(me, options);
            mergedOptions.buttons = {
                yes: Lobibox.base.OPTIONS.buttons.yes,
                no: Lobibox.base.OPTIONS.buttons.no
            };
            options = $.extend({}, mergedOptions, Lobibox.confirm.DEFAULTS, options);
            return options;
        },

        _init: function () {
            var me = this;

            LobiboxBase._init.call(me);
            me.show();
        },

        _afterShow: function () {
            var me = this;

            var d = $('<div></div>');
            if (me.$options.iconClass) {
                d.append($('<div class="lobibox-icon-wrapper"></div>')
                    .append('<i class="lobibox-icon ' + me.$options.iconClass + '"></i>'))
                ;
            }
            d.append('<div class="lobibox-body-text-wrapper"><span class="lobibox-body-text">' + me.$options.msg + '</span></div>');
            me._setContent(d.html());

            me._position();

            LobiboxBase._afterShow.call(me);
        }
    });

    Lobibox.confirm.DEFAULTS = {
        title: 'Question',
        width: 500
    };
//------------------------------------------------------------------------------
//-------------------------LobiboxAlert------------------------------------------
//------------------------------------------------------------------------------
    function LobiboxAlert(type, options) {
        this.$type = type;

//        options = $.extend({}, Lobibox.alert.DEFAULT_OPTIONS, Lobibox[type].DEFAULT_OPTIONS, options);

        this.$options = this._processInput(options);

        this._init();
        this.debug(this);
    }

    LobiboxAlert.prototype = $.extend({}, LobiboxBase, {
        constructor: LobiboxAlert,

        _processInput: function (options) {

//            ALERT_OPTIONS = $.extend({}, LobiboxAlert.OPTIONS, Lobibox.alert.DEFAULTS);
            var me = this;
            var mergedOptions = LobiboxBase._processInput.call(me, options);
            mergedOptions.buttons = {
                ok: Lobibox.base.OPTIONS.buttons.ok
            };

            options = $.extend({}, mergedOptions, Lobibox.alert.OPTIONS[me.$type], Lobibox.alert.DEFAULTS, options);

            return options;
        },

        _init: function () {
            var me = this;
            LobiboxBase._init.call(me);
            me.show();
        },

        _afterShow: function () {
            var me = this;

            var d = $('<div></div>');
            if (me.$options.iconClass) {
                d.append($('<div class="lobibox-icon-wrapper"></div>')
                    .append('<i class="lobibox-icon ' + me.$options.iconClass + '"></i>'))
                ;
            }
            d.append('<div class="lobibox-body-text-wrapper"><span class="lobibox-body-text">' + me.$options.msg + '</span></div>');
            me._setContent(d.html());
            me._position();

            LobiboxBase._afterShow.call(me);
        }
    });
    Lobibox.alert.OPTIONS = {
        warning: {
            title: 'Warning'
        },
        info: {
            title: 'Information'
        },
        success: {
            title: 'Success'
        },
        error: {
            title: 'Error'
        }
    };
    //User can set default options by this variable
    Lobibox.alert.DEFAULTS = {};
//------------------------------------------------------------------------------
//-------------------------LobiboxProgress--------------------------------------
//------------------------------------------------------------------------------
    function LobiboxProgress(options) {
        this.$type = 'progress';
        this.$progressBarElement = null;
        this.$options = this._processInput(options);
        this.$progress = 0;

        this._init();
        this.debug(this);
    }

    LobiboxProgress.prototype = $.extend({}, LobiboxBase, {
        constructor: LobiboxProgress,

        _processInput: function (options) {
            var me = this;
            var mergedOptions = LobiboxBase._processInput.call(me, options);

            options = $.extend({}, mergedOptions, Lobibox.progress.DEFAULTS, options);
            return options;
        },

        _init: function () {
            var me = this;

            LobiboxBase._init.call(me);
            me.show();
        },

        _afterShow: function () {
            var me = this;

            if (me.$options.progressTpl) {
                me.$progressBarElement = $(me.$options.progressTpl);
            } else {
                me.$progressBarElement = me._createProgressbar();
            }
            var label;
            if (me.$options.label) {
                label = $('<label>' + me.$options.label + '</label>');
            }
            var innerHTML = $('<div></div>').append(label, me.$progressBarElement);
            me._setContent(innerHTML);
            me._position();

            LobiboxBase._afterShow.call(me);
        },

        _createProgressbar: function () {
            var me = this;
            var outer = $('<div class="lobibox-progress-bar-wrapper lobibox-progress-outer"></div>')
                .append('<div class="lobibox-progress-bar lobibox-progress-element"></div>')
            ;
            if (me.$options.showProgressLabel) {
                outer.append('<span class="lobibox-progress-text" data-role="progress-text"></span>');
            }

            return outer;
        },

        /**
         * Set progress value
         *
         * @param {number} progress "progress value"
         * @returns {LobiboxProgress}
         */
        setProgress: function (progress) {
            var me = this;
            if (me.$progress === 100) {
                return;
            }
            progress = Math.min(100, Math.max(0, progress));
            me.$progress = progress;
            me._triggerEvent('progressUpdated');
            if (me.$progress === 100) {
                me._triggerEvent('progressCompleted');
            }
            me.$el.find('.lobibox-progress-element').css('width', progress.toFixed(1) + "%");
            me.$el.find('[data-role="progress-text"]').html(progress.toFixed(1) + "%");
            return me;
        },

        /**
         * Get progress value
         *
         * @returns {number}
         */
        getProgress: function () {
            return this.$progress;
        }
    });

    Lobibox.progress.DEFAULTS = {
        width: 500,
        showProgressLabel: true,  // Show percentage of progress
        label: '',  // Show progress label
        progressTpl: false,  //Template of progress bar

        //Events
        progressUpdated: null,
        progressCompleted: null
    };
//------------------------------------------------------------------------------
//-------------------------LobiboxWindow----------------------------------------
//------------------------------------------------------------------------------
    function LobiboxWindow(type, options) {
        this.$type = type;

        this.$options = this._processInput(options);

        this._init();
        this.debug(this);
    }

    LobiboxWindow.prototype = $.extend({}, LobiboxBase, {
        constructor: LobiboxWindow,
        _processInput: function (options) {
            var me = this;
            var mergedOptions = LobiboxBase._processInput.call(me, options);

            if (options.content && typeof options.content === 'function') {
                options.content = options.content();
            }
            if (options.content instanceof jQuery) {
                options.content = options.content.clone();
            }
            options = $.extend({}, mergedOptions, Lobibox.window.DEFAULTS, options);
            return options;
        },

        _init: function () {
            var me = this;

            LobiboxBase._init.call(me);
            me.setContent(me.$options.content);
            if (me.$options.url && me.$options.autoload) {
                if (!me.$options.showAfterLoad) {
                    me.show();
                }
                me.load(function () {
                    if (me.$options.showAfterLoad) {
                        me.show();
                    }
                });
            } else {
                me.show();
            }
        },

        _afterShow: function () {
            var me = this;

            me._position();

            LobiboxBase._afterShow.call(me);
        },

        /**
         * Setter method for <code>params</code> option
         *
         * @param {object} params "new params"
         * @returns {LobiboxWindow}
         */
        setParams: function (params) {
            var me = this;
            me.$options.params = params;
            return me;
        },
        /**
         * Getter method for <code>params</code>
         *
         * @returns {object}
         */
        getParams: function () {
            var me = this;
            return me.$options.params;
        },
        /**
         * Setter method of <code>loadMethod</code> option
         *
         * @param {string} method "new method"
         * @returns {LobiboxWindow}
         */
        setLoadMethod: function (method) {
            var me = this;
            me.$options.loadMethod = method;
            return me;
        },
        /**
         * Getter method for <code>loadMethod</code> option
         *
         * @returns {string}
         */
        getLoadMethod: function () {
            var me = this;
            return me.$options.loadMethod;
        },
        /**
         * Setter method of <code>content</code> option.
         * Change the content of window
         *
         * @param {string} content "new content"
         * @returns {LobiboxWindow}
         */
        setContent: function (content) {
            var me = this;
            me.$options.content = content;
            me.$el.find('.lobibox-body').html('').append(content);
            return me;
        },
        /**
         * Getter method of <code>content</code> option
         *
         * @returns {string}
         */
        getContent: function () {
            var me = this;
            return me.$options.content;
        },
        /**
         * Setter method of <code>url</code> option
         *
         * @param {string} url "new url"
         * @returns {LobiboxWindow}
         */
        setUrl: function (url) {
            this.$options.url = url;
            return this;
        },
        /**
         * Getter method of <code>url</code> option
         *
         * @returns {String}
         */
        getUrl: function () {
            return this.$options.url;
        },
        /**
         * Loads content to window by ajax from specific url
         *
         * @param {Function} callback "callback function"
         * @returns {LobiboxWindow}
         */
        load: function (callback) {
            var me = this;
            if (!me.$options.url) {
                return me;
            }
            $.ajax(me.$options.url, {
                method: me.$options.loadMethod,
                data: me.$options.params
            }).done(function (res) {
                me.setContent(res);
                if (callback && typeof callback === 'function') {
                    callback(res);
                }
            });
            return me;
        }
    });

    Lobibox.window.DEFAULTS = {
        width: 480,
        height: 600,
        content: '',  // HTML Content of window
        url: '',  // URL which will be used to load content
        draggable: true,  // Override default option
        autoload: true,  // Auto load from given url when window is created
        loadMethod: 'GET',  // Ajax method to load content
        showAfterLoad: true,  // Show window after content is loaded or show and then load content
        params: {}  // Parameters which will be send by ajax for loading content
    };

})();

//Author      : @arboshiki
/**
 * Generates random string of n length.
 * String contains only letters and numbers
 *
 * @param {int} n
 * @returns {String}
 */
Math.randomString = function (n) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < n; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
};
var Lobibox = Lobibox || {};
(function () {

    var LobiboxNotify = function (type, options) {
//------------------------------------------------------------------------------
//----------------PROTOTYPE VARIABLES-------------------------------------------
//------------------------------------------------------------------------------
        this.$type = null;
        this.$options = null;
        this.$el = null;
//------------------------------------------------------------------------------
//-----------------PRIVATE VARIABLES--------------------------------------------
//------------------------------------------------------------------------------        
        var me = this;
//------------------------------------------------------------------------------
//-----------------PRIVATE FUNCTIONS--------------------------------------------
//------------------------------------------------------------------------------
        var _processInput = function (options) {

            if (options.size === 'mini' || options.size === 'large') {
                options = $.extend({}, Lobibox.notify.OPTIONS[options.size], options);
            }
            options = $.extend({}, Lobibox.notify.OPTIONS[me.$type], Lobibox.notify.DEFAULTS, options);

            if (options.size !== 'mini' && options.title === true) {
                options.title = Lobibox.notify.OPTIONS[me.$type].title;
            } else if (options.size === 'mini' && options.title === true) {
                options.title = false;
            }
            if (options.icon === true) {
                options.icon = Lobibox.notify.OPTIONS.icons[options.iconSource][me.$type];
            }
            if (options.sound === true) {
                options.sound = Lobibox.notify.OPTIONS[me.$type].sound;
            }
            if (options.sound) {
                options.sound = options.soundPath + options.sound + options.soundExt;
            }
            return options;
        };

        var _appendInWrapper = function ($el, $wrapper) {
            if (me.$options.size === 'normal') {
                if ($wrapper.hasClass('bottom')) {
                    $wrapper.prepend($el);
                } else {
                    $wrapper.append($el);
                }

            } else if (me.$options.size === 'mini') {
                if ($wrapper.hasClass('bottom')) {
                    $wrapper.prepend($el);
                } else {
                    $wrapper.append($el);
                }
            } else if (me.$options.size === 'large') {
                var tabPane = _createTabPane().append($el);
                var $li = _createTabControl(tabPane.attr('id'));
                $wrapper.find('.lb-notify-wrapper').append(tabPane);
                $wrapper.find('.lb-notify-tabs').append($li);
                _activateTab($li);
                $li.find('>a').click(function () {
                    _activateTab($li);
                });
            }
        };
        var _activateTab = function ($li) {
            $li.closest('.lb-notify-tabs').find('>li').removeClass('active');
            $li.addClass('active');
            var $current = $($li.find('>a').attr('href'));
            $current.closest('.lb-notify-wrapper').find('>.lb-tab-pane').removeClass('active');
            $current.addClass('active')
        };
        var _createTabControl = function (tabPaneId) {
            var $li = $('<li></li>', {
                'class': Lobibox.notify.OPTIONS[me.$type]['class']
            });
            $('<a></a>', {
                'href': '#' + tabPaneId
            }).append('<i class="tab-control-icon ' + me.$options.icon + '"></i>')
                .appendTo($li);
            return $li;
        };
        var _createTabPane = function () {
            return $('<div></div>', {
                'class': 'lb-tab-pane',
                'id': Math.randomString(10)
            })
        };
        var _createNotifyWrapper = function () {
            var selector = (me.$options.size === 'large' ? '.lobibox-notify-wrapper-large' : '.lobibox-notify-wrapper')
                + "." + me.$options.position.replace(/\s/gi, '.'),
                $wrapper;

            //var classes = me.$options.position.split(" ");
            $wrapper = $(selector);
            if ($wrapper.length === 0) {
                $wrapper = $('<div></div>')
                    .addClass(selector.replace(/\./g, ' ').trim())
                    .appendTo($('body'));
                if (me.$options.size === 'large') {
                    $wrapper.append($('<ul class="lb-notify-tabs"></ul>'))
                        .append($('<div class="lb-notify-wrapper"></div>'));
                }
            }
            return $wrapper;
        };
        var _createNotify = function () {
            var OPTS = Lobibox.notify.OPTIONS,
                $iconEl,
                $innerIconEl,
                $iconWrapper,
                $body,
                $msg,
                $notify = $('<div></div>', {
                    'class': 'lobibox-notify ' + OPTS[me.$type]['class'] + ' ' + OPTS['class'] + ' ' + me.$options.showClass
                });

            $iconWrapper = $('<div class="lobibox-notify-icon-wrapper"></div>').appendTo($notify);
            $iconEl = $('<div class="lobibox-notify-icon"></div>').appendTo($iconWrapper);
            $innerIconEl = $('<div></div>').appendTo($iconEl);

            // Add image or icon depending on given parameters
            if (me.$options.img) {
                $innerIconEl.append('<img src="' + me.$options.img + '"/>');
            } else if (me.$options.icon) {
                $innerIconEl.append('<div class="icon-el"><i class="' + me.$options.icon + '"></i></div>');
            } else {
                $notify.addClass('without-icon');
            }
            // Create body, append title and message in body and append body in notification
            $msg = $('<div class="lobibox-notify-msg">' + me.$options.msg + '</div>');

            if (me.$options.messageHeight !== false) {
                $msg.css('max-height', me.$options.messageHeight);
            }

            $body = $('<div></div>', {
                'class': 'lobibox-notify-body'
            }).append($msg).appendTo($notify);

            if (me.$options.title) {
                $body.prepend('<div class="lobibox-notify-title">' + me.$options.title + '<div>');
            }
            _addCloseButton($notify);
            if (me.$options.size === 'normal' || me.$options.size === 'mini') {
                _addCloseOnClick($notify);
                _addDelay($notify);
            }

            // Give width to notification
            if (me.$options.width) {
                $notify.css('width', _calculateWidth(me.$options.width));
            }

            return $notify;
        };
        var _addCloseButton = function ($el) {
            if (!me.$options.closable) {
                return;
            }
            $('<span class="lobibox-close">&times;</span>')
                .click(function (ev) {
                    ev.preventDefault();
                    ev.stopPropagation();
                    me.remove();
                }).appendTo($el);
        };
        var _addCloseOnClick = function ($el) {
            if (!me.$options.closeOnClick) {
                return;
            }
            $el.click(function () {
                me.remove();
            });
        };
        var _addDelay = function ($el) {
            if (!me.$options.delay) {
                return;
            }
            if (me.$options.delayIndicator) {
                var delay = $('<div class="lobibox-delay-indicator"><div></div></div>');
                $el.append(delay);
            }
            var time = 0;
            var interval = 1000 / 30;
            var currentTime = new Date().getTime();
            var timer = setInterval(function () {
                if (me.$options.continueDelayOnInactiveTab) {
                    time = new Date().getTime() - currentTime;
                } else {
                    time += interval;
                }

                var width = 100 * time / me.$options.delay;
                if (width >= 100) {
                    width = 100;
                    me.remove();
                    timer = clearInterval(timer);
                }
                if (me.$options.delayIndicator) {
                    delay.find('div').css('width', width + "%");
                }

            }, interval);

            if (me.$options.pauseDelayOnHover) {
                $el.on('mouseenter.lobibox', function () {
                    interval = 0;
                }).on('mouseleave.lobibox', function () {
                    interval = 1000 / 30;
                });
            }
        };
        var _findTabToActivate = function ($li) {
            var $itemToActivate = $li.prev();
            if ($itemToActivate.length === 0) {
                $itemToActivate = $li.next();
            }
            if ($itemToActivate.length === 0) {
                return null;
            }
            return $itemToActivate;
        };
        var _calculateWidth = function (width) {
            width = Math.min($(window).outerWidth(), width);
            return width;
        };
//------------------------------------------------------------------------------
//----------------PROTOTYPE FUNCTIONS-------------------------------------------
//------------------------------------------------------------------------------
        /**
         * Delete the notification
         *
         * @returns {LobiboxNotify}
         */
        this.remove = function () {
            me.$el.removeClass(me.$options.showClass)
                .addClass(me.$options.hideClass);
            var parent = me.$el.parent();
            var wrapper = parent.closest('.lobibox-notify-wrapper-large');

            var href = '#' + parent.attr('id');

            var $li = wrapper.find('>.lb-notify-tabs>li:has(a[href="' + href + '"])');
            $li.addClass(Lobibox.notify.OPTIONS['class'])
                .addClass(me.$options.hideClass);
            setTimeout(function () {
                if (me.$options.size === 'normal' || me.$options.size === 'mini') {
                    me.$el.remove();
                } else if (me.$options.size === 'large') {

                    var $newLi = _findTabToActivate($li);
                    if ($newLi) {
                        _activateTab($newLi);
                    }
                    $li.remove();
                    parent.remove();
                }
                var list = Lobibox.notify.list;
                var ind = list.indexOf(me);
                list.splice(ind, 1);
                var next = list[ind];
                if (next && next.$options.showAfterPrevious) {
                    next._init();
                }
            }, 500);
            return me;
        };
        me._init = function () {
            // Create notification
            var $notify = _createNotify();
            if (me.$options.size === 'mini') {
                $notify.addClass('notify-mini');
            }

            if (typeof me.$options.position === 'string') {
                var $wrapper = _createNotifyWrapper();
                _appendInWrapper($notify, $wrapper);
                if ($wrapper.hasClass('center')) {
                    $wrapper.css('margin-left', '-' + ($wrapper.width() / 2) + "px");
                }
            } else {
                $('body').append($notify);
                $notify.css({
                    'position': 'fixed',
                    left: me.$options.position.left,
                    top: me.$options.position.top
                })
            }

            me.$el = $notify;
            if (me.$options.sound) {
                var snd = new Audio(me.$options.sound); // buffers automatically when created
                snd.play();
            }
            if (me.$options.rounded) {
                me.$el.addClass('rounded');
            }
            me.$el.on('click.lobibox', function (ev) {
                if (me.$options.onClickUrl) {
                    window.location.href = me.$options.onClickUrl;
                }
                if (me.$options.onClick && typeof me.$options.onClick === 'function') {
                    me.$options.onClick.call(me, ev);
                }
            });
            me.$el.data('lobibox', me);
        };
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
        this.$type = type;
        this.$options = _processInput(options);
        if (!me.$options.showAfterPrevious || Lobibox.notify.list.length === 0) {
            this._init();
        }

    };

    Lobibox.notify = function (type, options) {
        if (["default", "info", "warning", "error", "success"].indexOf(type) > -1) {
            var lobibox = new LobiboxNotify(type, options);
            Lobibox.notify.list.push(lobibox);
            return lobibox;
        }
    };
    Lobibox.notify.list = [];
    Lobibox.notify.closeAll = function () {
        var list = Lobibox.notify.list;
        for (var i in list) {
            list[i].remove();
        }
    };
    //User can set default options to this variable
    Lobibox.notify.DEFAULTS = {
        title: true,                // Title of notification. If you do not include the title in options it will automatically takes its value 
        //from Lobibox.notify.OPTIONS object depending of the type of the notifications or set custom string. Set this false to disable title
        size: 'normal',             // normal, mini, large
        soundPath: 'sounds/',   // The folder path where sounds are located
        soundExt: '.ogg',           // Default extension for all sounds
        showClass: 'fadeInDown',    // Show animation class.
        hideClass: 'zoomOut',       // Hide animation class.
        icon: true,                 // Icon of notification. Leave as is for default icon or set custom string
        msg: '',                    // Message of notification
        img: null,                  // Image source string
        closable: true,             // Make notifications closable
        hideCloseButton: false,     // Notification may be closable but you can hide close button and it will be closed by clicking on notification itsef
        delay: 5000,                // Hide notification after this time (in miliseconds)
        delayIndicator: true,       // Show timer indicator
        closeOnClick: true,         // Close notifications by clicking on them
        width: 400,                 // Width of notification box
        sound: true,                // Sound of notification. Set this false to disable sound. Leave as is for default sound or set custom soud path
        // Place to show notification. Available options: "top left", "top right", "bottom left", "bottom right", "center top", "center bottom"
        // It can also be object {left: number, top: number} to position notification at any place
        position: "bottom right",
        iconSource: 'bootstrap',    // "bootstrap" or "fontAwesome" the library which will be used for icons
        rounded: false,             // Whether to make notification corners rounded
        messageHeight: 60,          // Notification message maximum height. This is not for notification itself, this is for <code>.lobibox-notify-msg</code>
        pauseDelayOnHover: true,    // When you mouse over on notification delay (if it is enabled) will be paused.
        onClickUrl: null,           // The url which will be opened when notification is clicked
        showAfterPrevious: false,   // Set this to true if you want notification not to be shown until previous notification is closed. This is useful for notification queues
        continueDelayOnInactiveTab: true, // Continue delay when browser tab is inactive

        // Events
        onClick: null
    };
    //This variable is necessary.
    Lobibox.notify.OPTIONS = {
        'class': 'animated-fast',
        large: {
            width: 500,
            messageHeight: 96
        },
        mini: {
            'class': 'notify-mini',
            messageHeight: 32
        },
        default: {
            'class': 'lobibox-notify-default',
            'title': 'Default',
            sound: false
        },
        success: {
            'class': 'lobibox-notify-success',
            'title': 'Success',
            sound: 'sound2'
        },
        error: {
            'class': 'lobibox-notify-error',
            'title': 'Error',
            sound: 'sound4'
        },
        warning: {
            'class': 'lobibox-notify-warning',
            'title': 'Warning',
            sound: 'sound5'
        },
        info: {
            'class': 'lobibox-notify-info',
            'title': 'Information',
            sound: 'sound6'
        },
        icons: {
            bootstrap: {
                success: 'glyphicon glyphicon-ok-sign',
                error: 'glyphicon glyphicon-remove-sign',
                warning: 'glyphicon glyphicon-exclamation-sign',
                info: 'glyphicon glyphicon-info-sign'
            },
            fontAwesome: {
                success: 'fa fa-check-circle',
                error: 'fa fa-times-circle',
                warning: 'fa fa-exclamation-circle',
                info: 'fa fa-info-circle'
            }
        }
    };
})();


