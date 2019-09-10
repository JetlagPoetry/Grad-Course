function im = myConv2(image, filter)
filterlength = size(filter)
%zero padding
if mod(filterlength(1) ,2)==0
    image = cat(1,zeros(filterlength(1)/2-1,size(image,2)),image,zeros(filterlength(1)/2,size(image,2)))
else
    image = cat(1,zeros((filterlength(1)-1)/2,size(image,2)),image,zeros((filterlength(1)-1)/2,size(image,2)))
end
if mod(filterlength(2) ,2)==0
    image = cat(2,zeros(size(image,1),filterlength(2)/2-1),image,zeros(size(image,1),filterlength(2)/2))
else
    image = cat(2,zeros(size(image,1),(filterlength(2)-1)/2),image,zeros(size(image,1),(filterlength(2)-1)/2))
end
%convert the filter by 180 degrees
filter = rot90(filter,2)
%take dot product
for x = 1 : size(image,1)-size(filter,1)+1
    for y = 1 : size(image,2)-size(filter,2)+1
        im(x,y) = sum(sum(image(x:x+size(filter,1)-1, y:y+size(filter,2)-1).*filter))
    end
end
end