$(function() {
  'use strict';

  var croppingImage = document.querySelector('#croppingImage'),
  img_w = document.querySelector('.img-w'),
  cropBtn = document.querySelector('.crop'),
  croppedImg = document.querySelector('.cropped-img'),
  dwn = document.querySelector('.download'),
  upload = document.querySelector('#cropperImageUpload'),
  cropper = '';

  cropper = new Cropper(croppingImage, {
    zoomable: false
  });

  // on change show image with crop options
  upload.addEventListener('change', function (e) {
    if (e.target.files.length) {
      console.log(e.target.files[0]);
      var fileType = e.target.files[0].type;
      if(fileType === 'image/gif' || fileType === 'image/jpeg' || fileType === 'image/png') {
        cropper.destroy();
        // start file reader
        const reader = new FileReader();
        reader.onload = function (e) {
          if(e.target.result){
            croppingImage.src = e.target.result;
            cropper = new Cropper(croppingImage, {
              zoomable: false
            });
          }
        };
        reader.readAsDataURL(e.target.files[0]);
      } else {
        alert("Selected file type is not supported. Please try again")
      }
    }
  });

  // crop on click
  cropBtn.addEventListener('click',function(e) {
    e.preventDefault();
    // get result to data uri
    let imgSrc = cropper.getCroppedCanvas({
      width: img_w.value // input value
    }).toDataURL();
    croppedImg.src = imgSrc;
    dwn.setAttribute('href', imgSrc);
    dwn.download = 'imagename.png';
  });

});