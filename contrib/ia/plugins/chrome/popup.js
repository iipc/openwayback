$(function()
{
  chrome.extension.sendMessage({"type": "LIST_WAYBACKS"}, function(response) {
    if (response && response.waybacks) {
      var options = $("#waybacks");
      $.each(response.waybacks, function() {
        options.append($("<option/>").val(this.waybackUrl).text(this.waybackUrl));
      });
      
      var currWayback = response.currWayback;
      
      updateColls(currWayback);
      
      var currWBID = '#waybacks option[value="' + currWayback.waybackUrl + '"]';
      $(currWBID).attr("selected", "selected");
      
      $('#clientRewrite').prop('checked', response.opts.isClientRewrite);
      $('#proxyMode').prop('checked', response.opts.isProxyMode);
      $('#redirectErrors').prop('checked', response.opts.isRedirectErrors);
    }
  });
  
  function updateColls(currWayback)
  {
    if (currWayback.collections.length > 1) {
      var options = $("#colls");
      $.each(currWayback.collections, function() {
        options.append($("<option/>").val(this).text(this));
      });
      $("#collsDiv").show();
    } else {
      $("#collsDiv").hide();
    }
  }
  
  $("input[type=checkbox]").click(function(param)
  {
    chrome.extension.sendMessage({"type": "TOGGLE_OPT", "id": $(this).attr("id"), "enabled": this.checked});
  });
  
  $("#waybacks").change(function()
  {
    var index = $("#waybacks")[0].selectedIndex;
    chrome.extension.sendMessage({"type": "SEL_WAYBACK", "index": index}, function(response)
    {
      updateColls(response.currWayback);
    });
  });
  
});
