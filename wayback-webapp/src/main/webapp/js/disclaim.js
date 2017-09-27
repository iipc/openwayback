(function () {
    // Custom event listener for browser compatibility
    function addEvent(el, type, handler) {
        if (el.attachEvent) el.attachEvent('on' + type, handler); else el.addEventListener(type, handler);
    }

    // Wait until DOM is loaded to add disclaimer
    addEvent(document, 'DOMContentLoaded', function () {
        var body = document.body;
        var disclaimElem = document.createElement('div');
        var notice =
            '<div id="wm-ipp-disclaimer" ' +
            'data-body-bg-pos-y ' +
            'style="position:relative; z-index:99999; border:1px solid; color:black; ' +
            'background-color:lightYellow; font-size:10px; font-family:sans-serif; padding:5px;">' +
            window.wmNotice +
            ' [ <a id="wm-ipp-disclaimer-hide" style="color:blue; font-size:10px; ' +
            'text-decoration:underline;" href="javascript:void(0)">' + window.wmHideNotice + '</a> ]' +
            '</div>';

        function getFrameArea(frame) {
            if (frame.innerWidth) return frame.innerWidth * frame.innerHeight;
            if (frame.document.documentElement && frame.document.documentElement.clientHeight) return frame.document.documentElement.clientWidth * frame.document.documentElement.clientHeight;
            if (frame.document.body) return frame.document.body.clientWidth * frame.document.body.clientHeight;
            return 0;
        }

        // If body has style that offsets from top, shift down to compensate for disclaimer
        function shiftBody() {
            var disclaimMargin = parseInt(disclaimElem.marginTop, 10) + parseInt(disclaimElem.marginBottom, 10) || 0;
            var disclaimHeight = disclaimElem.offsetHeight + disclaimMargin + 'px';
            var bodyStyle = window.getComputedStyle ? getComputedStyle(body, null) : body.currentStyle;
            var bodyBgPosY = bodyStyle['background-position-y'] || '0px';
            var bodyPaddingTop = bodyStyle['padding-top'] || '0px';
            var bodyMarginTop = bodyStyle['margin-top'] || '0px';


            // background-position-y
            disclaimElem.setAttribute('data-body-bg-pos-y', bodyBgPosY);
            body.style['background-position-y'] = 'calc(' + disclaimHeight + ' + ' + bodyBgPosY + ')';

            // padding-top
            disclaimElem.setAttribute('data-padding-top', bodyPaddingTop);
            body.style['padding-top'] = 0;
            disclaimElem.style['padding-bottom'] = bodyPaddingTop;

            // margin-top
            disclaimElem.setAttribute('data-margin-top', bodyMarginTop);
            body.style['margin-top'] = 0;
            disclaimElem.style['margin-bottom'] = bodyMarginTop;
        }

        // Move the body back up when the disclaimer is closed
        function unshiftBody() {
            var bodyBgPosY = disclaimElem.getAttribute('data-body-bg-pos-y');
            var bodyPaddingTop = disclaimElem.getAttribute('data-padding-top');
            var bodyMarginTop = disclaimElem.getAttribute('data-margin-top');

            body.style['background-position-y'] = bodyBgPosY;
            body.style['padding-top'] = bodyPaddingTop;
            body.style['margin-top'] = bodyMarginTop;
        }

        // Hide disclaimer and shift body if previously shifted
        function hideDisclaimer() {
            disclaimElem.style.display = 'none';
            unshiftBody();
        }

        function disclaim() {
            if (top !== self) {
                if (top.document.body.tagName === 'BODY') {
                    return;
                }

                var largestArea = 0;
                var largestFrame = null;
                var i;
                for (i = 0; i < top.frames.length; i++) {
                    var frame = top.frames[i];
                    var area = getFrameArea(frame);

                    if (area > largestArea) {
                        largestFrame = frame;
                        largestArea = area;
                    }
                }
                if (self !== largestFrame) {
                    return;
                }
            }

            // Browser-compatible add class to disclaimer element
            if (disclaimElem.classList) disclaimElem.classList.add('wm-ipp-disclaimer-container');
            else disclaimElem.className = 'wm-ipp-disclaimer-container';

            // Add notice HTML to disclaimer element
            disclaimElem.innerHTML = notice;

            // Bind hide functionality to link
            var disclaimerHide = disclaimElem.querySelector('#wm-ipp-disclaimer-hide');
            addEvent(disclaimerHide, 'click', hideDisclaimer);

            // Add to DOM
            document.body.insertBefore(disclaimElem, document.body.firstChild);
            shiftBody();
        }

        disclaim();
    });
}());
