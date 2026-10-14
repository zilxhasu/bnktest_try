const puppeteer = require('puppeteer-core');
const [,, html, pdf] = process.argv;
(async () => {
  const b = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new' });
  const p = await b.newPage();
  await p.setViewport({ width: 1280, height: 720 });
  await p.goto('file://' + require('path').resolve(html), { waitUntil: 'networkidle0' });
  // overflow check: any element whose content exceeds its slide
  const issues = await p.evaluate(() => [...document.querySelectorAll('.slide')].flatMap((s, i) => {
    const sr = s.getBoundingClientRect(); const out = [];
    s.querySelectorAll('*').forEach(el => { const r = el.getBoundingClientRect();
      if (r.width && (r.bottom > sr.bottom - 30 || r.right > sr.right + 1) && !el.closest('.foot')) out.push(`slide ${i+1}: <${el.tagName.toLowerCase()} class="${el.className.baseVal ?? el.className}"> bottom=${Math.round(r.bottom - sr.top)} right=${Math.round(r.right - sr.left)}`); });
    return out.slice(0, 3); }));
  console.log(issues.join('\n') || 'no overflow');
  await p.pdf({ path: pdf, width: '1280px', height: '720px', printBackground: true });
  await b.close();
})();
