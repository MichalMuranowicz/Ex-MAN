(function() {
  function collect(win, path, out) {
    try {
      const doc = win.document;
      const bodyText = (doc.body && doc.body.innerText) ? doc.body.innerText : '';
      const rows = Array.from(doc.querySelectorAll('tr')).map(function(row) {
        return row.innerText || '';
      });
      out.frames.push({
        path: path,
        url: win.location.href,
        title: doc.title || '',
        text: bodyText,
        rows: rows
      });
      for (let i = 0; i < win.frames.length; i++) {
        collect(win.frames[i], path + '/frame[' + i + ']', out);
      }
    } catch(e) {
      out.frames.push({path:path, error:String(e), text:'', rows:[]});
    }
  }

  function collectUnits(win, path, units) {
    try {
      const doc = win.document;
      Array.from(doc.querySelectorAll('tr')).forEach(function(row) {
        const cells = Array.from(row.querySelectorAll('td')).map(function(cell) {
          return (cell.innerText || '').trim();
        });
        const nonEmpty = cells.filter(Boolean);
        if (nonEmpty[0] !== '20') return;
        if (!/^[A-Z0-9]{6,8}$/.test(nonEmpty[1] || '')) return;

        const link = row.querySelector("a[href*='transportDetails'], a[href*='transport']");
        units.push({
          frame: path,
          vehicleNo: nonEmpty[1] || '',
          pickupCode: nonEmpty[2] || '',
          pickupDate: nonEmpty[3] || '',
          country: nonEmpty[4] || '',
          dest: nonEmpty[5] || '',
          toNo: nonEmpty[16] || '',
          href: link ? link.href : '',
          cells: nonEmpty
        });
      });

      for (let i = 0; i < win.frames.length; i++) {
        collectUnits(win.frames[i], path + '/frame[' + i + ']', units);
      }
    } catch(e) {}
  }

  const out = {frames: []};
  collect(window, 'top', out);
  const bodyText = out.frames.map(function(frame) { return frame.text || ''; }).join('\n');
  const units = [];
  collectUnits(window, 'top', units);

  return JSON.stringify({
    url: location.href,
    title: document.title,
    hasVehicNo: bodyText.indexOf('Vehic. no') !== -1,
    hasPickup: bodyText.indexOf('Pick up') !== -1,
    units: units,
    unitCount: units.length,
    frames: out.frames.map(function(frame) {
      return {
        path: frame.path,
        url: frame.url || '',
        title: frame.title || '',
        textLength: (frame.text || '').length,
        rows: (frame.rows || []).length,
        error: frame.error || ''
      };
    })
  });
})();
