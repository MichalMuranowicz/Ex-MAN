(function() {
  const label = "__LABEL__";

  function clickIn(win) {
    try {
      const doc = win.document;
      const candidates = Array.from(doc.querySelectorAll('a, span, td, div, input, button'));
      for (const el of candidates) {
        const text = ((el.innerText || el.value || '') + '').trim();
        if (text !== label) continue;

        const clickable = el.closest('a') || el;
        clickable.click();
        return true;
      }

      for (let i = 0; i < win.frames.length; i++) {
        if (clickIn(win.frames[i])) return true;
      }
    } catch (e) {}
    return false;
  }

  return clickIn(window);
})();
