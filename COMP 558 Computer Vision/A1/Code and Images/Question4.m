%chop and display a region of original image and edge map
original_img = imread('image_gray.jpg');
original_img = imcrop(original_img,[90 18 20 20]);
edge_map = imread('0.5_magnitude.jpg');
edge_map = imcrop(edge_map,[90 18 20 20]);
figure;
imshow(original_img);
figure;
imshow(edge_map);

%overlay the quiver plot on the original image
[Fx, Fy] = gradient(double(original_img));
xgrid = 1:21;
ygrid = 1:21;
[X,Y] = meshgrid(xgrid,ygrid);
figure;
imagesc(original_img);
hold on;
quiver(Y',X',Fx(xgrid,ygrid),Fy(xgrid,ygrid));
hold off;
