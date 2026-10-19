from PIL import Image, ImageDraw, ImageFont
import math, pathlib, random
p=pathlib.Path('/mnt/data/MuseoDeLosMuseos/app/src/main/res/drawable')
W,H=960,640
font_path='/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf'
try: F=ImageFont.truetype(font_path,42)
except: F=ImageFont.load_default()
NAVY=(5,41,57); GOLD=(189,153,92); PAPER=(240,221,182); TEAL=(26,121,128)

def base(theme='night'):
 im=Image.new('RGB',(W,H));pix=im.load()
 for y in range(H):
  mix=y/H
  top=(12,60,81) if theme=='night' else (205,229,232)
  end=(5,29,43) if theme=='night' else (138,178,174)
  v=tuple(int(top[i]*(1-mix)+end[i]*mix) for i in range(3))
  ImageDraw.Draw(im).line((0,y,W,y),fill=v)
 return im,ImageDraw.Draw(im)

def museum():
 im,d=base();d.ellipse((610,-130,1050,330),fill=(31,78,90));
 # floor and warm light
 d.polygon([(0,545),(960,520),(960,640),(0,640)],fill=(23,47,55))
 d.polygon([(158,160),(480,25),(804,160)],fill=GOLD)
 d.polygon([(196,160),(480,55),(767,160)],fill=PAPER)
 d.rectangle((180,164,782,185),fill=GOLD)
 d.rectangle((193,190,773,495),fill=(228,214,177))
 for x in range(220,757,105):
  d.ellipse((x-14,180,x+66,220),fill=GOLD)
  d.rectangle((x,219,x+53,462),fill=(253,236,195))
  d.rectangle((x-8,461,x+61,485),fill=GOLD)
 d.rectangle((170,494,790,513),fill=GOLD)
 d.rectangle((160,520,802,542),fill=PAPER)
 # arch
 d.arc((422,299,563,502),180,360,fill=NAVY,width=15)
 d.rectangle((435,410,549,495),fill=NAVY)
 im.save(p/'cover_museum.png')

def book():
 im,d=base('light')
 d.ellipse((165,480,800,620),fill=(91,122,119))
 d.polygon([(195,145),(478,100),(718,147),(750,510),(470,550),(219,520)],fill=(91,49,33))
 d.polygon([(230,160),(482,124),(700,160),(716,486),(473,525),(239,490)],fill=(143,88,61))
 d.rectangle((309,210,616,220),fill=GOLD)
 d.text((312,239),'ARTE Y',font=F,fill=PAPER)
 d.text((280,305),'PATRIMONIO',font=F,fill=PAPER)
 d.rectangle((310,375,630,390),fill=GOLD)
 d.ellipse((440,413,485,460),outline=GOLD,width=7)
 im.save(p/'cover_book.png')

def kit():
 im,d=base('light')
 d.rounded_rectangle((175,200,785,514),40,fill=(214,181,112),outline=(121,88,59),width=6)
 d.rounded_rectangle((235,144,725,272),28,outline=(117,73,52),width=22)
 d.rounded_rectangle((195,245,767,492),24,fill=(230,208,158))
 for i,col in enumerate([(67,111,128),(161,84,56),(74,129,103)]):
  x=255+i*145
  d.rounded_rectangle((x,294,x+105,446),14,fill=col)
  d.line((x+20,335,x+86,335),fill=PAPER,width=7)
  d.line((x+20,367,x+86,367),fill=PAPER,width=7)
 d.rectangle((453,228,511,275),fill=(101,65,47))
 im.save(p/'cover_kit.png')

def course():
 im,d=base()
 d.rounded_rectangle((125,130,845,506),35,fill=(219,222,207),outline=GOLD,width=10)
 d.rounded_rectangle((160,163,810,461),25,fill=(24,76,95))
 d.polygon([(330,220),(330,408),(547,314)],fill=(237,201,132))
 for x in range(184,797,70):
  d.line((x,470,x+45,470),fill=GOLD,width=6)
 d.rectangle((409,514,561,552),fill=GOLD)
 d.rounded_rectangle((348,548,620,569),9,fill=PAPER)
 im.save(p/'cover_course.png')

def merch():
 im,d=base('light')
 d.polygon([(260,220),(700,220),(748,539),(209,539)],fill=(228,211,170))
 d.arc((351,95,590,333),180,360,fill=(94,67,54),width=24)
 d.line((351,215,351,360),fill=(94,67,54),width=18)
 d.line((590,215,590,360),fill=(94,67,54),width=18)
 d.polygon([(343,329),(480,258),(622,329)],fill=NAVY)
 for x in range(370,603,54):
  d.rectangle((x,345,x+34,431),fill=NAVY)
 d.rectangle((351,441,608,454),fill=NAVY)
 im.save(p/'cover_merch.png')
for fn in [museum,book,kit,course,merch]:fn()
print('Generated images',list(p.glob('*.png')))
